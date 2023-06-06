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

package bisq.protocol.poc.liquid_swap;

import bisq.network.NetworkIdWithKeyPair;
import bisq.network.NetworkService;
import bisq.persistence.PersistenceClient;
import bisq.protocol.SellerProtocol;
import bisq.protocol.poc.PocProtocolStore;
import bisq.protocol.poc.TakerPocProtocolModel;
import com.google.protobuf.Message;

public class LiquidSwapTakerAsSellerProtocol extends LiquidSwapTakerProtocol implements SellerProtocol {
    public LiquidSwapTakerAsSellerProtocol(NetworkService networkService,
                                           PersistenceClient<PocProtocolStore> persistenceClient,
                                           TakerPocProtocolModel protocolModel,
                                           NetworkIdWithKeyPair myNodeIdAndKeyPair) {
        super(networkService,
                persistenceClient,
                protocolModel,
                myNodeIdAndKeyPair);
    }

    @Override
    public Message toProto() {
        //todo
        return null;
    }
}