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

package bisq.user.banned;

import bisq.common.application.Service;
import bisq.common.observable.collection.ObservableSet;
import bisq.network.NetworkService;
import bisq.network.identity.NetworkId;
import bisq.network.p2p.services.data.DataService;
import bisq.network.p2p.services.data.storage.auth.authorized.AuthorizedData;
import bisq.persistence.Persistence;
import bisq.persistence.PersistenceClient;
import bisq.persistence.PersistenceService;
import bisq.user.profile.UserProfile;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

@Slf4j
public class BannedUserService implements PersistenceClient<BannedUserStore>, Service, DataService.Listener {
    @Getter
    private final BannedUserStore persistableStore = new BannedUserStore();
    @Getter
    private final Persistence<BannedUserStore> persistence;
    private final NetworkService networkService;

    public BannedUserService(PersistenceService persistenceService,
                             NetworkService networkService) {
        persistence = persistenceService.getOrCreatePersistence(this, persistableStore);
        this.networkService = networkService;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////
    // Service
    ///////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public CompletableFuture<Boolean> initialize() {
        networkService.getDataService().ifPresent(service -> service.getAuthorizedData().forEach(this::onAuthorizedDataAdded));
        networkService.addDataServiceListener(this);
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public CompletableFuture<Boolean> shutdown() {
        networkService.removeDataServiceListener(this);
        return CompletableFuture.completedFuture(true);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////
    // DataService.Listener
    ///////////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onAuthorizedDataAdded(AuthorizedData authorizedData) {
        if (authorizedData.getAuthorizedDistributedData() instanceof BannedUserProfileData) {
            BannedUserProfileData bannedUserProfileData = (BannedUserProfileData) authorizedData.getAuthorizedDistributedData();
            getBannedUserProfileDataSet().add(bannedUserProfileData);
            persist();
        }
    }

    @Override
    public void onAuthorizedDataRemoved(AuthorizedData authorizedData) {
        if (authorizedData.getAuthorizedDistributedData() instanceof BannedUserProfileData) {
            BannedUserProfileData bannedUserProfileData = (BannedUserProfileData) authorizedData.getAuthorizedDistributedData();
            getBannedUserProfileDataSet().remove(bannedUserProfileData);
            persist();
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////
    // API
    ///////////////////////////////////////////////////////////////////////////////////////////////////

    public ObservableSet<BannedUserProfileData> getBannedUserProfileDataSet() {
        return persistableStore.getBannedUserProfileDataSet();
    }

    public boolean isUserProfileBanned(String userProfileId) {
        return getBannedUserProfileDataSet().stream().anyMatch(e -> e.getUserProfile().getId().equals(userProfileId));
    }

    public boolean isUserProfileBanned(UserProfile userProfile) {
        return getBannedUserProfileDataSet().stream().anyMatch(e -> e.getUserProfile().equals(userProfile));
    }

    public boolean isNetworkIdBanned(NetworkId networkId) {
        return getBannedUserProfileDataSet().stream().anyMatch(e -> e.getUserProfile().getNetworkId().equals(networkId));
    }
}