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

import bisq.network.p2p.message.EnvelopePayloadMessage;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode
@Getter
public class MockMessage implements EnvelopePayloadMessage {
    private final String msg;

    public MockMessage(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "MockMessage{" +
                "\r\n     msg='" + msg + '\'' +
                "\r\n}";
    }

    @Override
    public double getCostFactor() {
        return 0;
    }

    @Override
    public bisq.network.protobuf.EnvelopePayloadMessage toProto() {
        return null;
    }
}
