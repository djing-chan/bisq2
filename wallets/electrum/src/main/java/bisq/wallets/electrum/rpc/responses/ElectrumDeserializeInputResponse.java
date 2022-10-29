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

package bisq.wallets.electrum.rpc.responses;

import com.squareup.moshi.Json;
import lombok.Getter;

@Getter
public class ElectrumDeserializeInputResponse {
    private boolean coinbase;
    @Json(name = "nsequence")
    private long nSequence;

    @Json(name = "prevout_hash")
    private String prevOutHash;
    @Json(name = "prevout_n")
    private int prevOutN;

    private String scriptSig;
    private String witness;
}
