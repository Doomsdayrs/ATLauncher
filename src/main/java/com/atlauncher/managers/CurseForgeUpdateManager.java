/*
 * ATLauncher - https://github.com/ATLauncher/ATLauncher
 * Copyright (C) 2013-2022 ATLauncher
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package com.atlauncher.managers;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.atlauncher.data.Instance;
import com.atlauncher.data.curseforge.CurseForgeFile;
import com.atlauncher.data.curseforge.CurseForgeProject;
import com.atlauncher.utils.CurseForgeApi;

import io.reactivex.rxjava3.subjects.BehaviorSubject;

public class CurseForgeUpdateManager {
    // CurseForge instance update checking
    private static final Map<String, BehaviorSubject<Optional<CurseForgeFile>>>
        CURSEFORGE_INSTANCE_LATEST_VERSION = new HashMap<>();

    public static BehaviorSubject<Optional<CurseForgeFile>> getSubject(Instance instance) {
        CURSEFORGE_INSTANCE_LATEST_VERSION.putIfAbsent(
            instance.id,
            BehaviorSubject.createDefault(Optional.empty())
        );
        return CURSEFORGE_INSTANCE_LATEST_VERSION.get(instance.id);
    }

    public static CurseForgeFile getLatestVersion(Instance instance) {
        return getSubject(instance).getValue().orElse(null);
    }

    public static void checkForUpdates() {
        if (ConfigManager.getConfigItem("platforms.curseforge.modpacksEnabled", true) == false) {
            return;
        }

        PerformanceManager.start();
        LogManager.info("Checking for updates to CurseForge instances");

        int[] projectIdsFound = InstanceManager.getInstances().parallelStream()
            .filter(i -> i.isCurseForgePack() && i.hasCurseForgeProjectId())
            .mapToInt(i -> i.launcher.curseForgeManifest != null
                ? i.launcher.curseForgeManifest.projectID
                : i.launcher.curseForgeProject.id)
            .toArray();

        Map<Integer, CurseForgeProject> foundProjects = CurseForgeApi.getProjectsAsMap(projectIdsFound);

        if (foundProjects != null) {

            InstanceManager.getInstances().parallelStream()
                .filter(i -> i.isCurseForgePack() && i.hasCurseForgeProjectId()).forEach(i -> {
                    CurseForgeProject curseForgeMod = foundProjects.get(i.launcher.curseForgeManifest != null
                        ? i.launcher.curseForgeManifest.projectID
                        : i.launcher.curseForgeProject.id);

                    if (curseForgeMod == null) {
                        return;
                    }

                    CurseForgeFile latestVersion = curseForgeMod.latestFiles.stream()
                        .sorted(Comparator.comparingInt((
                            CurseForgeFile file) -> file.id).reversed())
                        .findFirst().orElse(null);

                    getSubject(i).onNext(Optional.ofNullable(latestVersion));
                });
        }

        PerformanceManager.end();
    }
}
