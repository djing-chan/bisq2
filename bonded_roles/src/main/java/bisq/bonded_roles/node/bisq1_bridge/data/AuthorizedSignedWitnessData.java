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

package bisq.bonded_roles.node.bisq1_bridge.data;

import bisq.common.application.DevMode;
import bisq.common.proto.ProtoResolver;
import bisq.common.proto.UnresolvableProtobufMessageException;
import bisq.network.p2p.services.data.storage.DistributedData;
import bisq.network.p2p.services.data.storage.MetaData;
import bisq.network.p2p.services.data.storage.auth.authorized.AuthorizedDistributedData;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@EqualsAndHashCode
@Getter
public final class AuthorizedSignedWitnessData implements AuthorizedDistributedData {
    public final static long TTL = TimeUnit.DAYS.toMillis(15);

    // The pubKeys which are authorized for publishing that data.
    // todo Production key not set yet - we use devMode key only yet
    private static final Set<String> authorizedPublicKeys = Set.of();

    private final MetaData metaData = new MetaData(TTL,
            100000,
            AuthorizedSignedWitnessData.class.getSimpleName());

    private final String profileId;
    private final long witnessSignDate;

    public AuthorizedSignedWitnessData(String profileId, long witnessSignDate) {
        this.profileId = profileId;
        this.witnessSignDate = witnessSignDate;
    }

    @Override
    public bisq.bonded_roles.protobuf.AuthorizedSignedWitnessData toProto() {
        return bisq.bonded_roles.protobuf.AuthorizedSignedWitnessData.newBuilder()
                .setProfileId(profileId)
                .setWitnessSignDate(witnessSignDate)
                .build();
    }

    public static AuthorizedSignedWitnessData fromProto(bisq.bonded_roles.protobuf.AuthorizedSignedWitnessData proto) {
        return new AuthorizedSignedWitnessData(
                proto.getProfileId(),
                proto.getWitnessSignDate());
    }

    public static ProtoResolver<DistributedData> getResolver() {
        return any -> {
            try {
                return fromProto(any.unpack(bisq.bonded_roles.protobuf.AuthorizedSignedWitnessData.class));
            } catch (InvalidProtocolBufferException e) {
                throw new UnresolvableProtobufMessageException(e);
            }
        };
    }

    @Override
    public MetaData getMetaData() {
        return metaData;
    }

    @Override
    public boolean isDataInvalid(byte[] pubKeyHash) {
        return false;
    }

    @Override
    public Set<String> getAuthorizedPublicKeys() {
        if (DevMode.isDevMode()) {
            return DevMode.AUTHORIZED_DEV_PUBLIC_KEYS;
        } else {
            return authorizedPublicKeys;
        }
    }

    @Override
    public String toString() {
        return "AuthorizedSignedWitnessData{" +
                ",\r\n     profileId=" + profileId +
                ",\r\n     witnessSignAge=" + new Date(witnessSignDate) +
                "\r\n}";
    }
}