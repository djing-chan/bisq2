/*
 * This file is part of Bisq.
 *
 * Bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bisq. If not, see <http://www.gnu.org/licenses/>.
 */

package bisq.network.p2p;


import bisq.common.observable.Observable;
import bisq.common.util.CompletableFutureUtils;
import bisq.network.NetworkService;
import bisq.network.common.Address;
import bisq.network.common.TransportType;
import bisq.network.identity.NetworkId;
import bisq.network.p2p.message.EnvelopePayloadMessage;
import bisq.network.p2p.node.CloseReason;
import bisq.network.p2p.node.Connection;
import bisq.network.p2p.node.Node;
import bisq.network.p2p.node.NodesById;
import bisq.network.p2p.node.network_load.NetworkLoadService;
import bisq.network.p2p.node.transport.TransportService;
import bisq.network.p2p.services.confidential.ConfidentialMessageListener;
import bisq.network.p2p.services.confidential.ConfidentialMessageService;
import bisq.network.p2p.services.confidential.MessageListener;
import bisq.network.p2p.services.confidential.ack.MessageDeliveryStatusService;
import bisq.network.p2p.services.data.DataNetworkService;
import bisq.network.p2p.services.data.DataService;
import bisq.network.p2p.services.peergroup.BanList;
import bisq.network.p2p.services.peergroup.PeerGroupManager;
import bisq.persistence.PersistenceService;
import bisq.security.KeyPairService;
import bisq.security.PubKey;
import com.runjva.sourceforge.jsocks.protocol.Socks5Proxy;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.security.KeyPair;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.runAsync;

/**
 * Creates nodesById, the default node and the services according to the Config.
 */
@Slf4j
public class ServiceNode {
    @Getter
    public static final class Config {
        public static Config from(com.typesafe.config.Config config) {
            return new Config(new HashSet<>(config.getEnumList(Service.class, "p2pServiceNode")));
        }

        private final Set<Service> services;

        public Config(Set<Service> services) {
            this.services = services;
        }
    }

    public interface Listener {
        void onStateChanged(ServiceNode.State state);
    }

    public enum State {
        NEW,

        INITIALIZE_TRANSPORT,
        TRANSPORT_INITIALIZED,

        INITIALIZE_DEFAULT_NODE,
        DEFAULT_NODE_INITIALIZED,

        INITIALIZE_PEER_GROUP,
        PEER_GROUP_INITIALIZED,

        STOPPING,
        TERMINATED
    }

    public enum Service {
        PEER_GROUP,
        DATA,
        CONFIDENTIAL,
        ACK,
        MONITOR
    }

    private final Config config;
    private final Node.Config nodeConfig;
    private final PeerGroupManager.Config peerGroupServiceConfig;
    private final Optional<DataService> dataService;
    private final Optional<MessageDeliveryStatusService> messageDeliveryStatusService;
    private final KeyPairService keyPairService;
    private final PersistenceService persistenceService;
    private final Set<Address> seedNodeAddresses;
    private final TransportType transportType;
    private final NetworkLoadService networkLoadService;

    @Getter
    private final NodesById nodesById;
    @Getter
    private final TransportService transportService;
    private final BanList banList = new BanList();

    @Getter
    private Node defaultNode;
    private final int defaultNodePort;
    @Getter
    private Optional<ConfidentialMessageService> confidentialMessageService = Optional.empty();
    @Getter
    private Optional<PeerGroupManager> peerGroupService = Optional.empty();
    @Getter
    private Optional<DataNetworkService> dataServicePerTransport = Optional.empty();
    private final Set<Listener> listeners = new CopyOnWriteArraySet<>();
    @Getter
    public Observable<State> state = new Observable<>(State.NEW);

    public ServiceNode(Config config,
                       Node.Config nodeConfig,
                       PeerGroupManager.Config peerGroupServiceConfig,
                       Optional<DataService> dataService,
                       Optional<MessageDeliveryStatusService> messageDeliveryStatusService,
                       KeyPairService keyPairService,
                       PersistenceService persistenceService,
                       Set<Address> seedNodeAddresses,
                       TransportType transportType,
                       NetworkLoadService networkLoadService) {
        this.config = config;
        this.nodeConfig = nodeConfig;
        this.peerGroupServiceConfig = peerGroupServiceConfig;
        this.messageDeliveryStatusService = messageDeliveryStatusService;
        this.dataService = dataService;
        this.keyPairService = keyPairService;
        this.persistenceService = persistenceService;
        this.seedNodeAddresses = seedNodeAddresses;
        this.transportType = transportType;
        this.networkLoadService = networkLoadService;

        transportService = TransportService.create(transportType, nodeConfig.getTransportConfig());
        nodesById = new NodesById(banList, nodeConfig, transportService, networkLoadService);
        defaultNodePort = nodeConfig.getTransportConfig().getDefaultNodePort();
    }

    public void createDefaultNode(NetworkId defaultNetworkId) {
        defaultNode = nodesById.createAndConfigNode(defaultNetworkId);

        Set<Service> services = config.getServices();
        peerGroupService = services.contains(Service.PEER_GROUP) ?
                Optional.of(new PeerGroupManager(persistenceService,
                        defaultNode,
                        banList,
                        peerGroupServiceConfig,
                        seedNodeAddresses)) :
                Optional.empty();

        boolean dataServiceEnabled = services.contains(Service.PEER_GROUP) && services.contains(Service.DATA);
        dataServicePerTransport = dataServiceEnabled ?
                Optional.of(dataService.orElseThrow().getDataServicePerTransport(transportType,
                        defaultNode,
                        peerGroupService.orElseThrow())) :
                Optional.empty();

        confidentialMessageService = services.contains(Service.CONFIDENTIAL) ?
                Optional.of(new ConfidentialMessageService(nodesById, keyPairService, dataService, messageDeliveryStatusService)) :
                Optional.empty();

        initializeTransport();
        initializeDefaultNode();
        initializePeerGroup();
    }

    private void initializeTransport() {
        setState(State.INITIALIZE_TRANSPORT);
        transportService.initialize();
        setState(State.TRANSPORT_INITIALIZED);
    }

    private void initializeDefaultNode() {
        setState(State.INITIALIZE_DEFAULT_NODE);
        defaultNode.initialize();
        setState(State.DEFAULT_NODE_INITIALIZED);
    }

    private void initializePeerGroup() {
        peerGroupService.ifPresent(peerGroupService -> {
            setState(State.INITIALIZE_PEER_GROUP);
            peerGroupService.initialize();
            setState(State.PEER_GROUP_INITIALIZED);
        });
    }

    Node getInitializedNode(NetworkId networkId) {
        return nodesById.getInitializedNode(networkId);
    }

    public CompletableFuture<Boolean> shutdown() {
        setState(State.STOPPING);
        return CompletableFutureUtils.allOf(
                        confidentialMessageService.map(ConfidentialMessageService::shutdown).orElse(completedFuture(true)),
                        peerGroupService.map(PeerGroupManager::shutdown).orElse(completedFuture(true)),
                        dataServicePerTransport.map(DataNetworkService::shutdown).orElse(completedFuture(true)),
                        nodesById.shutdown()
                )
                .orTimeout(10, TimeUnit.SECONDS)
                .handle((list, throwable) -> throwable == null && list.stream().allMatch(e -> e))
                .thenCompose(result -> transportService.shutdown())
                .whenComplete((result, throwable) -> setState(State.TERMINATED));
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////
    // API
    ///////////////////////////////////////////////////////////////////////////////////////////////////


    public boolean isNodeInitialized(NetworkId networkId) {
        return nodesById.isNodeInitialized(networkId);
    }

    public void addSeedNodeAddress(Address seedNodeAddress) {
        peerGroupService.ifPresent(peerGroupService -> peerGroupService.addSeedNodeAddress(seedNodeAddress));
    }

    public void removeSeedNodeAddress(Address seedNodeAddress) {
        peerGroupService.ifPresent(peerGroupService -> peerGroupService.removeSeedNodeAddress(seedNodeAddress));
    }

    public ConfidentialMessageService.SendConfidentialMessageResult confidentialSend(EnvelopePayloadMessage envelopePayloadMessage,
                                                                                     Address address,
                                                                                     PubKey receiverPubKey,
                                                                                     KeyPair senderKeyPair,
                                                                                     NetworkId senderNetworkId) {
        checkArgument(confidentialMessageService.isPresent(), "ConfidentialMessageService not present at confidentialSend");
        return confidentialMessageService.get().send(envelopePayloadMessage, address, receiverPubKey, senderKeyPair, senderNetworkId);
    }

    public Connection send(NetworkId senderNetworkId, EnvelopePayloadMessage envelopePayloadMessage, Address address) {
        return getNodesById().send(senderNetworkId, envelopePayloadMessage, address);
    }

    public void addMessageListener(MessageListener messageListener) {
        //todo rename NodeListener
        nodesById.addNodeListener(new Node.Listener() {
            @Override
            public void onMessage(EnvelopePayloadMessage envelopePayloadMessage, Connection connection, NetworkId networkId) {
                messageListener.onMessage(envelopePayloadMessage);
            }

            @Override
            public void onConnection(Connection connection) {
            }

            @Override
            public void onDisconnect(Connection connection, CloseReason closeReason) {
            }
        });
        confidentialMessageService.ifPresent(service -> service.addMessageListener(messageListener));
    }

    public void removeMessageListener(MessageListener messageListener) {
        //todo missing nodesById.removeNodeListener ?
        confidentialMessageService.ifPresent(service -> service.removeMessageListener(messageListener));
    }

    public void addConfidentialMessageListener(ConfidentialMessageListener listener) {
        confidentialMessageService.ifPresent(service -> service.addConfidentialMessageListener(listener));
    }

    public void removeConfidentialMessageListener(ConfidentialMessageListener listener) {
        confidentialMessageService.ifPresent(service -> service.removeConfidentialMessageListener(listener));
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    public Optional<Socks5Proxy> getSocksProxy() throws IOException {
        return defaultNode.getSocksProxy();
    }

    public Optional<Node> findNode(NetworkId networkId) {
        return nodesById.findNode(networkId);
    }

    private void setState(State newState) {
        if (newState == state.get()) {
            return;
        }
        checkArgument(state.get().ordinal() < newState.ordinal(),
                "New state %s must have a higher ordinal as the current state %s", newState, state.get());
        state.set(newState);
        log.info("New state {}", newState);
        runAsync(() -> listeners.forEach(e -> e.onStateChanged(newState)), NetworkService.DISPATCHER);
    }
}
