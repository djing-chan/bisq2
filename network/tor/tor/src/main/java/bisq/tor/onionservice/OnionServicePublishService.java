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

package bisq.tor.onionservice;

import bisq.network.identity.TorIdentity;
import bisq.tor.controller.NativeTorController;
import lombok.extern.slf4j.Slf4j;
import net.freehaven.tor.control.TorControlConnection;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
public class OnionServicePublishService {
    private final NativeTorController nativeTorController;
    private final Map<TorIdentity, CompletableFuture<OnionAddress>> onionAddressByNodeId = new HashMap<>();

    public OnionServicePublishService(NativeTorController nativeTorController, Path torDirPath) {
        this.nativeTorController = nativeTorController;
    }

    public synchronized CompletableFuture<OnionAddress> publish(TorIdentity torIdentity, int onionServicePort, int localPort) {
        boolean isNodeIdAlreadyPublished = onionAddressByNodeId.containsKey(torIdentity);
        if (isNodeIdAlreadyPublished) {
            return onionAddressByNodeId.get(torIdentity);
        }

        CompletableFuture<OnionAddress> completableFuture = new CompletableFuture<>();
        onionAddressByNodeId.put(torIdentity, completableFuture);

        try {
            String privateKey = torIdentity.getTorOnionKey();
            TorControlConnection.CreateHiddenServiceResult jTorResult =
                    nativeTorController.createHiddenService(onionServicePort, localPort, privateKey);

            var onionAddress = new OnionAddress(jTorResult.serviceID + ".onion", onionServicePort);
            completableFuture.complete(onionAddress);

        } catch (IOException e) {
            log.error("Couldn't initialize nodeId {}", torIdentity);
            completableFuture.completeExceptionally(e);
        }

        return completableFuture;
    }

    public synchronized CompletableFuture<Void> publish(TorIdentity torIdentity, int localPort) {
        try {
            String privateKey = torIdentity.getTorOnionKey();
            nativeTorController.createHiddenService(torIdentity.getPort(), localPort, privateKey);
            return CompletableFuture.completedFuture(null);

        } catch (IOException e) {
            log.error("Couldn't create onion service {}", torIdentity);
            return CompletableFuture.failedFuture(e);
        }
    }

    public synchronized Optional<OnionAddress> getOnionAddressForNode(String nodeId) {
        try {
            CompletableFuture<OnionAddress> completableFuture = onionAddressByNodeId.get(nodeId);
            if (completableFuture == null) {
                return Optional.empty();
            }

            OnionAddress onionAddress = completableFuture.get();
            return Optional.of(onionAddress);

        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
