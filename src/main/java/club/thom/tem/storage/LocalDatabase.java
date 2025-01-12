package club.thom.tem.storage;

import club.thom.tem.TEM;
import club.thom.tem.models.export.StoredUniqueItem;
import club.thom.tem.storage.services.UniqueItemService;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dizitart.no2.IndexOptions;
import org.dizitart.no2.IndexType;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.NitriteCollection;
import org.dizitart.no2.objects.ObjectRepository;

public class LocalDatabase {
    private static final Logger logger = LogManager.getLogger(LocalDatabase.class);
    private TEM tem;
    private String filePath = "/item/datastore";
    private Nitrite nitrite;
    private UniqueItemService uniqueItemService;
    private ObjectRepository<StoredUniqueItem> uniqueItemRepository;

    public LocalDatabase(TEM tem) {
        this.tem = tem;
    }


    public void initialize() {
        logger.info("Initializing local database");
        nitrite = openDatabase();
        logger.info("Creating indexes...");
        createIndexes();
        uniqueItemRepository = getDatabase().getRepository(StoredUniqueItem.class);
        uniqueItemService = new UniqueItemService(this);
        logger.info("Initialised!");
    }

    protected Nitrite openDatabase() {
        return Nitrite.builder()
                .compressed()
                .filePath(getFilePath())
                .openOrCreate();
    }

    public void commit() {
        getDatabase().commit();
    }

    public Nitrite getDatabase() {
        return nitrite;
    }

    public NitriteCollection getUniqueItemsCollection() {
        return getDatabase().getCollection("uniqueItems");
    }

    private void createIndexes() {
        NitriteCollection itemsCollection = getUniqueItemsCollection();
        if (!itemsCollection.hasIndex("uuid")) {
            itemsCollection.createIndex("uuid", IndexOptions.indexOptions(IndexType.Unique));
        }

        if (!itemsCollection.hasIndex("itemId")) {
            itemsCollection.createIndex("itemId", IndexOptions.indexOptions(IndexType.NonUnique));
        }

        if (!itemsCollection.hasIndex("location.position")) {
            itemsCollection.createIndex("location.position", IndexOptions.indexOptions(IndexType.NonUnique));
        }
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setFileDirectory(String fileDirectory) {
        setFilePath(fileDirectory + "datastore.db");
    }

    public TEM getTEM() {
        return tem;
    }

    public ObjectRepository<StoredUniqueItem> getUniqueItemRepository() {
        return uniqueItemRepository;
    }

    public UniqueItemService getUniqueItemService() {
        return uniqueItemService;
    }
}
