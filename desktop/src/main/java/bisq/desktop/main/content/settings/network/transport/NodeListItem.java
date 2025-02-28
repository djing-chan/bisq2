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

package bisq.desktop.main.content.settings.network.transport;

import bisq.desktop.common.threading.UIThread;
import bisq.desktop.components.table.TableItem;
import bisq.i18n.Res;
import bisq.identity.Identity;
import bisq.identity.IdentityService;
import bisq.network.common.Address;
import bisq.network.identity.NetworkId;
import bisq.network.p2p.message.EnvelopePayloadMessage;
import bisq.network.p2p.node.CloseReason;
import bisq.network.p2p.node.Connection;
import bisq.network.p2p.node.Node;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Slf4j
public class NodeListItem implements TableItem {
    @Getter
    private final Node node;
    @Getter
    private final StringProperty date = new SimpleStringProperty();
    @Getter
    private final String address;
    @EqualsAndHashCode.Include
    @Getter
    private final String nodeId;
    @Getter
    private final String type;
    @Getter
    private final String domainId;
    @Getter
    private final StringProperty numConnections = new SimpleStringProperty();
    private final Node.Listener listener;

    public NodeListItem(Node node, IdentityService identityService) {
        this.node = node;
        nodeId = node.getNetworkId().toString();
        type = identityService.findActiveIdentityByNetworkId(node.getNetworkId())
                .map(i -> Res.get("settings.network.nodes.type.active"))
                .or(() -> identityService.findRetiredIdentityByNetworkId(node.getNetworkId()).map(i -> Res.get("settings.network.nodes.type.retired")))
                .orElseGet(() -> nodeId.equals(Node.DEFAULT) ? Res.get("settings.network.nodes.type.gossip") : Res.get("data.na"));
        domainId = identityService.findAnyIdentityByNetworkId(node.getNetworkId()).map(Identity::getTag)
                .orElse(Res.get("data.na"));
        address = node.findMyAddress().map(Address::getFullAddress).orElse(Res.get("data.na"));

        numConnections.set(String.valueOf(node.getAllConnections().count()));

        listener = new Node.Listener() {
            @Override
            public void onMessage(EnvelopePayloadMessage envelopePayloadMessage, Connection connection, NetworkId networkId) {
            }

            @Override
            public void onConnection(Connection connection) {
                UIThread.run(() -> numConnections.set(String.valueOf(node.getAllConnections().count())));
            }

            @Override
            public void onDisconnect(Connection connection, CloseReason closeReason) {
                UIThread.run(() -> numConnections.set(String.valueOf(node.getAllConnections().count())));
            }

            @Override
            public void onStateChange(Node.State state) {
            }
        };
    }

    public int compareAddress(NodeListItem other) {
        return address.compareTo(other.getAddress());
    }

    public int compareNodeId(NodeListItem other) {
        return nodeId.compareTo(other.getNodeId());
    }

    public int compareType(NodeListItem other) {
        return type.compareTo(other.getType());
    }

    public int compareDomainId(NodeListItem other) {
        return domainId.compareTo(other.getDomainId());
    }

    public int compareNumConnections(NodeListItem other) {
        return Long.compare(node.getAllConnections().count(), other.getNode().getAllConnections().count());
    }

    @Override
    public void activate() {
        node.addListener(listener);
    }

    @Override
    public void deactivate() {
        node.removeListener(listener);
    }
}
