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

package bisq.social.user;

import bisq.common.observable.Observable;
import bisq.common.observable.ObservableSet;
import bisq.common.proto.ProtoResolver;
import bisq.common.proto.UnresolvableProtobufMessageException;
import bisq.identity.profile.UserProfile;
import bisq.persistence.PersistableStore;
import bisq.social.user.proof.Proof;
import bisq.social.user.proof.ProofOfBurnProof;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Persists my user profiles and the selected user profile.
 */
@Slf4j
@Getter
public final class ChatUserStore implements PersistableStore<ChatUserStore> {
    private final Observable<UserProfile> selectedChatUserIdentity = new Observable<>();
    private final ObservableSet<UserProfile> chatUserIdentities;
    private final Map<String, ProofOfBurnProof> verifiedProofOfBurnProofs = new HashMap<>();

    public ChatUserStore() {
        chatUserIdentities = new ObservableSet<>();
    }

    private ChatUserStore(UserProfile selectedUserProfile,
                          Set<UserProfile> chatUserIdentities,
                          Map<String, ProofOfBurnProof> verifiedProofOfBurnProofs) {
        this.selectedChatUserIdentity.set(selectedUserProfile);
        this.chatUserIdentities = new ObservableSet<>(chatUserIdentities);
        this.verifiedProofOfBurnProofs.putAll(verifiedProofOfBurnProofs);
    }

    @Override
    public bisq.social.protobuf.ChatUserStore toProto() {
        return bisq.social.protobuf.ChatUserStore.newBuilder()
                .setSelectedChatUserIdentity(selectedChatUserIdentity.get().toProto())
                .addAllChatUserIdentities(chatUserIdentities.stream().map(UserProfile::toProto).collect(Collectors.toSet()))
                .putAllVerifiedProofOfBurnProofs(verifiedProofOfBurnProofs.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().toProto())))
                .build();
    }

    public static ChatUserStore fromProto(bisq.social.protobuf.ChatUserStore proto) {
        return new ChatUserStore(UserProfile.fromProto(proto.getSelectedChatUserIdentity()),
                proto.getChatUserIdentitiesList().stream()
                        .map(UserProfile::fromProto)
                        .collect(Collectors.toSet()),
                proto.getVerifiedProofOfBurnProofsMap().entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey,
                                e -> (ProofOfBurnProof) Proof.fromProto(e.getValue()))));
    }

    @Override
    public ProtoResolver<PersistableStore<?>> getResolver() {
        return any -> {
            try {
                return fromProto(any.unpack(bisq.social.protobuf.ChatUserStore.class));
            } catch (InvalidProtocolBufferException e) {
                throw new UnresolvableProtobufMessageException(e);
            }
        };
    }

    @Override
    public ChatUserStore getClone() {
        return new ChatUserStore(selectedChatUserIdentity.get(), chatUserIdentities, verifiedProofOfBurnProofs);
    }

    @Override
    public void applyPersisted(ChatUserStore persisted) {
        selectedChatUserIdentity.set(persisted.getSelectedChatUserIdentity().get());
        chatUserIdentities.addAll(persisted.getChatUserIdentities());
        verifiedProofOfBurnProofs.putAll(persisted.getVerifiedProofOfBurnProofs());
    }
}