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

package bisq.desktop.common.view;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public enum NavigationTarget {
    NONE(),
    ROOT(),

    PRIMARY_STAGE(ROOT),
    
    SPLASH(PRIMARY_STAGE),
    
    ONBOARDING(PRIMARY_STAGE),

    INIT_USER_PROFILE(ONBOARDING),
    SELECT_USER_TYPE(ONBOARDING),
    ONBOARD_NEWBIE(ONBOARDING),
    ONBOARD_PRO_TRADER(ONBOARDING),
   
    MAIN(PRIMARY_STAGE),
   
    CONTENT(MAIN),
   
    SOCIAL(CONTENT),
    
    CHAT(SOCIAL),
    USER_PROFILE(SOCIAL),

    TRADE(CONTENT),
    OFFERBOOK(TRADE),
    CREATE_OFFER(TRADE),
    TAKE_OFFER(TRADE, false),

    PORTFOLIO(CONTENT),
    OPEN_OFFERS(PORTFOLIO),
    PENDING_TRADES(PORTFOLIO),
    CLOSED_TRADES(PORTFOLIO),

    EDUCATION(CONTENT),
    EVENTS(CONTENT),
    
    SETTINGS(CONTENT),
    PREFERENCES(SETTINGS),
    NETWORK_INFO(SETTINGS),
    ABOUT(SETTINGS),

    MARKETS(CONTENT),

    WALLET(CONTENT),
    WALLET_TRANSACTIONS(WALLET),
    WALLET_SEND(WALLET),
    WALLET_RECEIVE(WALLET),
    WALLET_UTXOS(WALLET);

    @Getter
    private final Optional<NavigationTarget> parent;
    @Getter
    private final List<NavigationTarget> path;
    @Getter
    private final boolean allowPersistence;

    NavigationTarget() {
        parent = Optional.empty();
        path = new ArrayList<>();
        allowPersistence = true;
    }

    NavigationTarget(NavigationTarget parent) {
        this(parent, true);
    }

    NavigationTarget(NavigationTarget parent, boolean allowPersistence) {
        this.parent = Optional.of(parent);
        List<NavigationTarget> temp = new ArrayList<>();
        Optional<NavigationTarget> candidate = Optional.of(parent);
        while (candidate.isPresent()) {
            temp.add(0, candidate.get());
            candidate = candidate.get().getParent();
        }
        this.path = temp;
        this.allowPersistence = allowPersistence;
    }
}