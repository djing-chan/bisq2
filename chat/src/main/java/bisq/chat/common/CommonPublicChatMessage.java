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

package bisq.chat.common;

import bisq.chat.ChatChannelDomain;
import bisq.chat.ChatMessageType;
import bisq.chat.Citation;
import bisq.chat.pub.PublicChatMessage;
import bisq.common.util.StringUtils;
import bisq.network.p2p.services.data.storage.MetaData;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

import static bisq.network.p2p.services.data.storage.MetaData.MAX_MAP_SIZE_10_000;
import static bisq.network.p2p.services.data.storage.MetaData.TTL_10_DAYS;

@Slf4j
@Getter
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public final class CommonPublicChatMessage extends PublicChatMessage {
    private final MetaData metaData = new MetaData(TTL_10_DAYS, getClass().getSimpleName(), MAX_MAP_SIZE_10_000);

    public CommonPublicChatMessage(ChatChannelDomain chatChannelDomain,
                                   String channelId,
                                   String authorUserProfileId,
                                   String text,
                                   Optional<Citation> citation,
                                   long date,
                                   boolean wasEdited) {
        this(StringUtils.createUid(),
                chatChannelDomain,
                channelId,
                authorUserProfileId,
                Optional.of(text),
                citation,
                date,
                wasEdited,
                ChatMessageType.TEXT);
    }

    private CommonPublicChatMessage(String messageId,
                                    ChatChannelDomain chatChannelDomain,
                                    String channelId,
                                    String authorUserProfileId,
                                    Optional<String> text,
                                    Optional<Citation> citation,
                                    long date,
                                    boolean wasEdited,
                                    ChatMessageType chatMessageType) {
        super(messageId,
                chatChannelDomain,
                channelId,
                authorUserProfileId,
                text,
                citation,
                date,
                wasEdited,
                chatMessageType);

        // log.error("{} {}", metaData.getClassName(), toProto().getSerializedSize()); //755
    }

    public bisq.chat.protobuf.ChatMessage toProto() {
        return getChatMessageBuilder().setCommonPublicChatMessage(bisq.chat.protobuf.CommonPublicChatMessage.newBuilder()).build();
    }

    public static CommonPublicChatMessage fromProto(bisq.chat.protobuf.ChatMessage baseProto) {
        Optional<Citation> citation = baseProto.hasCitation() ?
                Optional.of(Citation.fromProto(baseProto.getCitation())) :
                Optional.empty();
        return new CommonPublicChatMessage(
                baseProto.getId(),
                ChatChannelDomain.fromProto(baseProto.getChatChannelDomain()),
                baseProto.getChannelId(),
                baseProto.getAuthorUserProfileId(),
                Optional.of(baseProto.getText()),
                citation,
                baseProto.getDate(),
                baseProto.getWasEdited(),
                ChatMessageType.fromProto(baseProto.getChatMessageType()));
    }

    @Override
    public double getCostFactor() {
        return 0.3;
    }
}