package club.thom.tem.export.search;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumChatFormatting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ContainerSearchResults extends Container {
    private static final ExecutorService executor = Executors.newFixedThreadPool(1, r -> new Thread(r, "TEMSearchFilter"));
    private static final Logger logger = LogManager.getLogger(ContainerSearchResults.class);
    AtomicInteger filtersRunning = new AtomicInteger(0);
    int totalSlots;
    final List<ClickableItem> allResults;
    List<ClickableItem> filteredResults;
    int skippedRows = 0;

    int selectedSortFilter = -1;
    private final List<SortFilter> sortFilters;
    private String lastFilterText = "";

    IInventory searchResultsInventory;
    public ContainerSearchResults(List<ClickableItem> items, List<SortFilter> sortFilters) {
        this.allResults = items;
        this.filteredResults = items;
        this.sortFilters = sortFilters;
        totalSlots = 54;
        searchResultsInventory = new InventoryBasic("Search Results", true, totalSlots);
        addSlots();
        fillPage();
        scrollTo(0.0F);
    }

    private void addSlots() {
        for (int j = 0; j < 5; ++j) {
            for (int k = 0; k < 9; ++k)
            {
                this.addSlotToContainer(new Slot(searchResultsInventory, k + j * 9, 9 + k * 18, 18 + j * 18));
            }
        }
        for (int i = 0; i < 9; i++) {
            this.addSlotToContainer(new Slot(searchResultsInventory, totalSlots - 9 + i, 9 + i * 18, 112));
        }
    }

    private ItemStack getSortButton() {
        if (sortFilters == null || sortFilters.isEmpty()) {
            return null;
        }

        ItemStack sortButton = new ItemStack(Item.getItemFromBlock(Blocks.anvil), 1);
        sortButton.setStackDisplayName("Sort");
        // Add lore
        NBTTagList lore = new NBTTagList();
        for (int i = 0; i < sortFilters.size(); i++) {
            if (i == selectedSortFilter) {
                lore.appendTag(new NBTTagString(EnumChatFormatting.GREEN + sortFilters.get(i).getName()));
            } else {
                lore.appendTag(new NBTTagString(EnumChatFormatting.GRAY + sortFilters.get(i).getName()));
            }
        }
        sortButton.getTagCompound().getCompoundTag("display").setTag("Lore", lore);
        return sortButton;
    }

    private void fillPage() {
        int finalRowId = totalSlots - 9;

        ItemStack sortButton = getSortButton();
        if (sortButton != null) {
            searchResultsInventory.setInventorySlotContents(finalRowId + 4, sortButton);
        }
    }

    private void applySort() {
        filteredResults.sort(sortFilters.get(selectedSortFilter).getComparator());
    }

    public void changeSortFilter() {
        selectedSortFilter++;
        if (selectedSortFilter >= sortFilters.size()) {
            selectedSortFilter = -1;
            // original sort
            filteredResults = allResults;
            if (!lastFilterText.isEmpty()) {
                // there was a filter, let's reapply it
                String filterText = this.lastFilterText;
                lastFilterText = "";
                setFilter(filterText);
            }
        } else {
            if (filteredResults == allResults) {
                filteredResults = new ArrayList<>(allResults);
            }
            applySort();
        }
        fillPage();
        scrollTo(0.0f);
    }

    public ItemStack slotClick(int slotId, int clickedButton, int mode, EntityPlayer playerIn) {
        if (slotId < 0) {
            return null;
        }

        if (slotId >= totalSlots - 9) {
            if (slotId == totalSlots - 5) {
                changeSortFilter();
            }
            return null;
        }

        int trueSlotNumber = (this.skippedRows*9) + slotId;
        if (filteredResults.size() <= trueSlotNumber) {
            return null;
        }
        filteredResults.get(trueSlotNumber).onClick();
        return null;
    }

    public void scrollTo(float scrollPosition) {
        int rowCount = (this.filteredResults.size() + 9 - 1) / 9 - 5;
        int rowIndex = (int)((double)(scrollPosition * (float)rowCount) + 0.5D);

        if (rowIndex < 0) {
            rowIndex = 0;
        }
        this.skippedRows = rowIndex;

        for (int onScreenRow = 0; onScreenRow < 5; ++onScreenRow) {
            for (int slotColumn = 0; slotColumn < 9; ++slotColumn) {
                int i = slotColumn + (onScreenRow + rowIndex) * 9;

                if (i >= 0 && i < this.filteredResults.size()) {
                    searchResultsInventory.setInventorySlotContents(slotColumn + onScreenRow * 9, this.filteredResults.get(i).getItem());
                }
                else
                {
                    searchResultsInventory.setInventorySlotContents(slotColumn + onScreenRow * 9, null);
                }
            }
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer playerIn) {
        return true;
    }

    public void setFilter(String filterText) {
        if (filterText == null || filterText.isEmpty()) {
            if (selectedSortFilter != -1) {
                filteredResults = new ArrayList<>(allResults);
                // was just filtered, might need to reapply sort
                applySort();
            } else {
                filteredResults = allResults;
            }
            lastFilterText = "";
            this.scrollTo(0.0f);
            return;
        }
        if (filterText.equals(lastFilterText)) {
            // No need to filter again
            return;
        }
        filterText = filterText.toLowerCase();
        if (lastFilterText.isEmpty() || !filterText.startsWith(lastFilterText)) {
            filteredResults = new ArrayList<>(allResults);
            if (selectedSortFilter != -1) {
                applySort();
            }
        }
        String finalFilterText = filterText;

        filtersRunning.incrementAndGet();
        executor.execute(() -> {
            try {
                if (filtersRunning.decrementAndGet() > 0) {
                    // Another filter started running, no point continuing
                    return;
                }
                List<ClickableItem> filteredOutput = new ArrayList<>(filteredResults.size());
                for (ClickableItem clickableItem : filteredResults) {
                    if (clickableItem.matchesFilter(finalFilterText)) {
                        filteredOutput.add(clickableItem);
                    }
                }
                filteredResults = filteredOutput;
                lastFilterText = finalFilterText;
                this.scrollTo(0.0F);
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Error filtering search results", e);
            }
        });
    }
}
