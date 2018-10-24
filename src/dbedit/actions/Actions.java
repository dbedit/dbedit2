package dbedit.actions;

/**
 * Created by IntelliJ IDEA.
 * User: ouwenlj
 * Date: 14-dec-2005
 * Time: 16:56:40
 */
public interface Actions {

    CustomAction CONNECT = new ConnectAction();
    CustomAction DISCONNECT = new DisconnectAction();
    CustomAction COMMIT = new CommitAction();
    CustomAction ROLLBACK = new RollbackAction();
    CustomAction UNDO = new UndoAction();
    CustomAction REDO = new RedoAction();
    CustomAction CUT = new CutAction();
    CustomAction COPY = new CopyAction();
    CustomAction PASTE = new PasteAction();
    CustomAction RUN = new RunAction();
    CustomAction RUN_SCRIPT = new RunScriptAction();
    CustomAction SCHEMA_BROWSER = new SchemaBrowserAction();
    CustomAction FAVORITES = new FavoritesAction();
    CustomAction HISTORY_PREVIOUS = new HistoryPreviousAction();
    CustomAction HISTORY_NEXT = new HistoryNextAction();
    CustomAction INSERT = new InsertAction();
    CustomAction EDIT = new EditAction();
    CustomAction DUPLICATE = new DuplicateAction();
    CustomAction DELETE = new DeleteAction();
    CustomAction LOB_OPEN = new LobOpenAction();
    CustomAction LOB_OPEN_WITH = new LobOpenWithAction();
    CustomAction LOB_IMPORT = new LobImportAction();
    CustomAction LOB_EXPORT = new LobExportAction();
    CustomAction LOB_COPY = new LobCopyAction();
    CustomAction LOB_PASTE = new LobPasteAction();
    GroupAction LOB_GROUP = new LobGroupAction();
    CustomAction EXPORT_EXCEL = new ExportExcelAction();
    CustomAction EXPORT_FLAT_FILE = new ExportFlatFileAction();
    CustomAction EXPORT_INSERTS = new ExportInsertsAction();
    GroupAction EXPORT_GROUP = new ExportGroupAction();
    CustomAction FETCH_LIMIT = new FetchLimitAction();
    CustomAction MANUAL = new ManualAction();
    CustomAction SELECT_FROM = new SelectFromAction();
    CustomAction ABOUT = new AboutAction();
    CustomAction UPDATE = new UpdateAction();
}
