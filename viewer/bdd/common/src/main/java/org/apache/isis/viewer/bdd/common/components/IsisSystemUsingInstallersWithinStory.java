package org.apache.isis.viewer.bdd.common.components;

import org.apache.isis.core.runtime.authentication.AuthenticationManagerInstaller;
import org.apache.isis.runtime.installers.InstallerLookup;
import org.apache.isis.runtime.persistence.PersistenceMechanismInstaller;
import org.apache.isis.runtime.system.DeploymentType;
import org.apache.isis.runtime.system.installers.IsisSystemUsingInstallers;
import org.apache.isis.runtime.userprofile.UserProfileStoreInstaller;
import org.apache.isis.runtime.userprofile.inmemory.InMemoryUserProfileStoreInstaller;

public class IsisSystemUsingInstallersWithinStory extends
        IsisSystemUsingInstallers {

    public IsisSystemUsingInstallersWithinStory(
            final DeploymentType deploymentType,
            final InstallerLookup installerLookup) {
        super(deploymentType, installerLookup);

        final AuthenticationManagerInstaller authManagerInstaller = new StoryAuthenticationManagerInstaller();
        setAuthenticationInstaller(getInstallerLookup().injectDependenciesInto(
                authManagerInstaller));

        final PersistenceMechanismInstaller persistorInstaller = new StoryInMemoryPersistenceMechanismInstaller();
        setPersistenceMechanismInstaller(getInstallerLookup()
                .injectDependenciesInto(persistorInstaller));

        final UserProfileStoreInstaller userProfileStoreInstaller = new InMemoryUserProfileStoreInstaller();
        setUserProfileStoreInstaller(getInstallerLookup()
                .injectDependenciesInto(userProfileStoreInstaller));
    }

}

// Copyright (c) Naked Objects Group Ltd.
