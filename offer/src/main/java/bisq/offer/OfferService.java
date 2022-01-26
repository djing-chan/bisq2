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

package bisq.offer;

import bisq.account.accounts.Account;
import bisq.account.protocol.SwapProtocolType;
import bisq.account.settlement.SettlementMethod;
import bisq.common.monetary.Market;
import bisq.common.monetary.Monetary;
import bisq.common.monetary.Quote;
import bisq.common.util.StringUtils;
import bisq.identity.IdentityService;
import bisq.network.NetworkId;
import bisq.network.NetworkIdWithKeyPair;
import bisq.network.NetworkService;
import bisq.network.p2p.services.data.broadcast.BroadcastResult;
import bisq.offer.options.ListingOption;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
public class OfferService {
    private final NetworkService networkService;
    private final IdentityService identityService;

    public OfferService(NetworkService networkService, IdentityService identityService) {
        this.networkService = networkService;
        this.identityService = identityService;
    }

    public CompletableFuture<Boolean> initialize() {
        CompletableFuture<Boolean> future = new CompletableFuture<>();
        future.complete(true);
        return future;
    }

    public void shutdown() {
    }

    public CompletableFuture<Offer> createOffer(Market selectedMarket,
                                                Direction direction,
                                                Monetary baseSideAmount,
                                                Quote fixPrice,
                                                SwapProtocolType selectedProtocolTyp,
                                                List<Account> selectedBaseSideAccounts,
                                                List<Account> selectedQuoteSideAccounts,
                                                List<SettlementMethod> selectedBaseSideSettlementMethods,
                                                List<SettlementMethod> selectedQuoteSideSettlementMethods) {
        String offerId = StringUtils.createUid();
        return identityService.getOrCreateIdentity(offerId).thenApply(identity ->
        {
            NetworkId makerNetworkId = identity.networkId();
            List<SwapProtocolType> protocolTypes = new ArrayList<>(List.of(selectedProtocolTyp));

            FixPrice priceSpec = new FixPrice(fixPrice.getValue());

            List<SettlementSpec> baseSideSettlementSpecs;
            if (!selectedBaseSideAccounts.isEmpty()) {
                baseSideSettlementSpecs = selectedBaseSideAccounts.stream()
                        .map(e -> new SettlementSpec(e.getSettlementMethod().name(), e.getId()))
                        .collect(Collectors.toList());
            } else {
                baseSideSettlementSpecs = selectedBaseSideSettlementMethods.stream()
                        .map(e -> new SettlementSpec(e.name(), null))
                        .collect(Collectors.toList());
            }
            List<SettlementSpec> quoteSideSettlementSpecs;
            if (!selectedBaseSideAccounts.isEmpty()) {
                quoteSideSettlementSpecs = selectedQuoteSideAccounts.stream()
                        .map(e -> new SettlementSpec(e.getSettlementMethod().name(), e.getId()))
                        .collect(Collectors.toList());
            } else {
                quoteSideSettlementSpecs = selectedQuoteSideSettlementMethods.stream()
                        .map(e -> new SettlementSpec(e.name(), null))
                        .collect(Collectors.toList());
            }

            List<ListingOption> listingOptions = new ArrayList<>();

            return new Offer(offerId,
                    new Date().getTime(),
                    makerNetworkId,
                    selectedMarket,
                    direction,
                    baseSideAmount.getValue(),
                    priceSpec,
                    protocolTypes,
                    baseSideSettlementSpecs,
                    quoteSideSettlementSpecs,
                    listingOptions
            );
        });
    }

    public CompletableFuture<CompletableFuture<List<CompletableFuture<BroadcastResult>>>> publishOffer(Offer offer) {
        return identityService.getOrCreateIdentity(offer.getId())
                .thenApply(identity -> {
                    NetworkIdWithKeyPair nodeIdAndKeyPair = identity.getNodeIdAndKeyPair();
                    return networkService.addData(offer, nodeIdAndKeyPair);
                });
    }
}