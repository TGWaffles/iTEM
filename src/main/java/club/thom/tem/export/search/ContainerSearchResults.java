package club.thom.tem.export.search;

import club.thom.tem.TEM;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.EnumDyeColor;
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
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class ContainerSearchResults extends Container {
    private static final ExecutorService executor = Executors.newFixedThreadPool(1, r -> new Thread(r, "TEMSearchFilter"));
    private static final Logger logger = LogManager.getLogger(ContainerSearchResults.class);
    AtomicInteger filtersRunning = new AtomicInteger(0);
    int totalSlots;
    final List<ClickableItem> allResults;
    List<ClickableItem> filteredResults;
    int skippedRows = 0;

    boolean enableRegex = TEM.getInstance().getConfig().enableRegexSearching();
    boolean onlyCurrentProfile = TEM.getInstance().getConfig().shouldLimitSearchToCurrentProfile();
    int selectedSortFilter = -1;
    private final List<SortFilter> sortFilters;
    private String lastFilterText = "";

    IInventory searchResultsInventory;
    public ContainerSearchResults(List<ClickableItem> items, List<SortFilter> sortFilters) {
        this.allResults = items;
        this.filteredResults = new ArrayList<>(items);
        this.sortFilters = sortFilters;
        totalSlots = 54;
        searchResultsInventory = new InventoryBasic("Search Results", true, totalSlots);
        addSlots();
        applyProfileFilter();
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

    private ItemStack getRegexButton() {
        ItemStack regexButton;
        if (enableRegex) {
            regexButton = new ItemStack(Items.dye, 1, EnumDyeColor.LIME.getDyeDamage());
            regexButton.setStackDisplayName("Regex ENABLED");
        } else {
            regexButton = new ItemStack(Items.dye, 1, EnumDyeColor.GRAY.getDyeDamage());
            regexButton.setStackDisplayName("Regex DISABLED");
        }
        return regexButton;
    }

    private ItemStack getOnlyCurrentProfileButton() {
        ItemStack onlyCurrentProfileButton;
        if (onlyCurrentProfile) {
            onlyCurrentProfileButton = new ItemStack(Item.getItemFromBlock(Blocks.redstone_block), 1);
            onlyCurrentProfileButton.setStackDisplayName("Only Current Profile");
        } else {
            onlyCurrentProfileButton = new ItemStack(Item.getItemFromBlock(Blocks.emerald_block), 1);
            onlyCurrentProfileButton.setStackDisplayName("All Profiles");
        }
        return onlyCurrentProfileButton;
    }

    private void fillPage() {
        int finalRowId = totalSlots - 9;

        ItemStack onlyCurrentProfileButton = getOnlyCurrentProfileButton();
        searchResultsInventory.setInventorySlotContents(finalRowId, onlyCurrentProfileButton);

        ItemStack sortButton = getSortButton();
        if (sortButton != null) {
            searchResultsInventory.setInventorySlotContents(finalRowId + 4, sortButton);
        }

        ItemStack regexButton = getRegexButton();
        searchResultsInventory.setInventorySlotContents(finalRowId + 8, regexButton);
    }

    private void applySort() {
        if (selectedSortFilter == -1) {
            return;
        }
        filteredResults.sort(sortFilters.get(selectedSortFilter).getComparator());
    }

    public void changeSortFilter() {
        selectedSortFilter++;
        if (selectedSortFilter >= sortFilters.size()) {
            selectedSortFilter = -1;
            // original sort
            resetAndReapplyFilters();
        } else {
            applySort();
        }
        fillPage();
        scrollTo(0.0f);
    }

    public void changeRegex() {
        enableRegex = !enableRegex;
        TEM.getInstance().getConfig().setRegexSearching(enableRegex);

        resetAndReapplyFilters();

        fillPage();
        scrollTo(0.0f);
    }

    public void changeOnlyCurrentProfile() {
        onlyCurrentProfile = !onlyCurrentProfile;
        TEM.getInstance().getConfig().setLimitSearchToCurrentProfile(onlyCurrentProfile);

        if (!onlyCurrentProfile) {
            // we are now showing all profiles, let's reapply the filter
            resetAndReapplyFilters();
        } else {
            applyProfileFilter();
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
            } else if (slotId == totalSlots - 1) {
                changeRegex();
            } else if (slotId == totalSlots - 9) {
                changeOnlyCurrentProfile();
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

    private void applyProfileFilter() {
        String profileId = TEM.getInstance().getProfileIdListener().getProfileId();
        if (onlyCurrentProfile && profileId != null) {
            filteredResults.removeIf(clickableItem -> !clickableItem.location.getProfileId().equals(profileId));
        }
    }

    private void resetAndReapplyFilters() {
        filteredResults = new ArrayList<>(allResults);
        applyProfileFilter();
        String lastKnownFilteredText = lastFilterText;
        lastFilterText = "";
        setFilter(lastKnownFilteredText);
        applySort();
    }

    public void setFilter(String filterText) {
        if (filterText == null || filterText.isEmpty()) {
            // Trying to set an empty filter.
            if (!lastFilterText.isEmpty()) {
                // Filter wasn't empty but now is. Reset and reapply filters
                lastFilterText = "";
                resetAndReapplyFilters();
                this.scrollTo(0.0f);
                return;
            }
            // Was empty, is empty. Do nothing.
            return;
        }
        // New filter text isn't empty. Let's filter.
        if (filterText.equalsIgnoreCase(lastFilterText)) {
            // No need to filter again, it's unchanged.
            return;
        }
        filterText = filterText.toLowerCase();
        if (!filterText.startsWith(lastFilterText)) {
            // New search. Set lastFilter to none (clear the filter), reset, and continue.
            lastFilterText = "";
            resetAndReapplyFilters();
        }
        String finalFilterText = filterText;

        filtersRunning.incrementAndGet();
        executor.execute(() -> {
            try {
                if (filtersRunning.decrementAndGet() > 0) {
                    // Another filter started running, no point continuing
                    return;
                }
                Pattern filterAsRegex = null;
                try {
                    filterAsRegex = Pattern.compile(finalFilterText, Pattern.CASE_INSENSITIVE);
                } catch (PatternSyntaxException ignored) {}
                List<ClickableItem> filteredOutput = new ArrayList<>(filteredResults.size());
                for (ClickableItem clickableItem : filteredResults) {
                    if (
                            clickableItem.matchesFilter(finalFilterText) ||
                            (TEM.getInstance().getConfig().enableRegexSearching() && filterAsRegex != null
                                    && clickableItem.matchesFilter(filterAsRegex))
                    ) {
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
