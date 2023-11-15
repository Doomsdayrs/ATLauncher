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
package com.atlauncher.gui.tabs.instances;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.mini2Dx.gettext.GetText;

import com.atlauncher.builders.HTMLBuilder;
import com.atlauncher.constants.UIConstants;
import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.gui.card.InstanceCard;
import com.atlauncher.gui.card.NilCard;
import com.atlauncher.gui.panels.HierarchyPanel;
import com.atlauncher.gui.tabs.InstancesTab;
import com.atlauncher.managers.PerformanceManager;
import com.atlauncher.utils.Pair;
import com.atlauncher.viewmodel.base.IInstancesTabViewModel;
import com.google.common.collect.Lists;

public final class InstancesListPanel extends HierarchyPanel
    implements RelocalizationListener {

    private final InstancesTab instancesTab;
    private final IInstancesTabViewModel viewModel;

    private final NilCard nilCard = new NilCard(
        getNilMessage(),
        new NilCard.Action[]{
            NilCard.Action.createCreatePackAction(),
            NilCard.Action.createDownloadPackAction()
        });

    public InstancesListPanel(InstancesTab instancesTab, final IInstancesTabViewModel viewModel) {
        super(new GridBagLayout());
        this.instancesTab = instancesTab;
        this.viewModel = viewModel;
        PerformanceManager.start("Displaying Instances");
    }

    private static String getNilMessage() {
        return new HTMLBuilder()
            .text(GetText.tr("There are no instances to display.<br/><br/>Install one from the Packs tab."))
            .build();
    }

    @Override
    protected void onShow() {
        addDisposable(viewModel.getInstancesList()
            .map(instancesList -> {
                    viewModel.setIsLoading(true);
                    return instancesList.instances.stream().map(instance ->
                        new InstanceCard(
                            instance.instance,
                            instance.hasUpdate,
                            instancesList.instanceTitleFormat
                        )
                    ).collect(Collectors.toList());
                }
            ).subscribe(newInstances -> {
                final GridBagConstraints gbc = new GridBagConstraints();
                gbc.gridx = gbc.gridy = 0;
                gbc.weightx = 1.0;
                gbc.insets = UIConstants.FIELD_INSETS;
                gbc.fill = GridBagConstraints.BOTH;

                if (newInstances.isEmpty()) {
                    removeAll();
                    this.add(this.nilCard, gbc);
                } else {
                    PerformanceManager.start("Render cards");

                    List<Pair<Integer, InstanceCard>> currentCards = new ArrayList<>();

                    // Generate current cards
                    {
                        Component[] components = this.getComponents();
                        int index = 0;
                        for (Component component : components) {
                            if (component instanceof InstanceCard) {
                                currentCards.add(new Pair<>(index, (InstanceCard) component));
                            }
                            index++;
                        }
                    }

                    // Update cards
                    {
                        List<Pair<Integer, InstanceCard>> updated = new ArrayList<>(currentCards);

                    }

                    // Remove
                    {
                        List<Pair<Integer, InstanceCard>> removed = new ArrayList<>(currentCards);
                        removed.removeIf(visibleCard -> {
                                InstanceCard currentCard = visibleCard.right();
                                return newInstances.stream()
                                    .noneMatch(newCard -> newCard.getInstance().uuid == currentCard.getInstance().uuid);
                            }
                        );
                        removed.forEach(pair -> remove(pair.left()));
                    }

                    removeAll();

                    // Portion up into chunks of 10, to make rendering easier
                    Lists.partition(newInstances, 10).forEach(subInstances -> {
                        newInstances.forEach(instance -> {
                            this.add(
                                instance,
                                gbc
                            );
                            gbc.gridy++;
                        });

                        validate();
                        repaint();
                    });
                    PerformanceManager.end("Render cards");
                }

                viewModel.setIsLoading(false); // Broken, reason above

                // After repainting is done, let scroll view resume
                invokeLater(() -> instancesTab.setScroll(viewModel.getScroll()));
                PerformanceManager.end("Displaying Instances");
            }));
    }

    @Override
    public void onRelocalization() {
        this.nilCard.setMessage(getNilMessage());
        nilCard.setActions(new NilCard.Action[]{
            NilCard.Action.createCreatePackAction(),
            NilCard.Action.createDownloadPackAction()
        });
    }

    @Override
    protected void createViewModel() {
    }

    @Override
    protected void onDestroy() {
        removeAll();
    }
}
