package dbedit.actions;

public class ExportGroupAction extends GroupAction {

    protected ExportGroupAction() {
        super("Export");
        addAction(Actions.EXPORT_EXCEL);
        addAction(Actions.EXPORT_PDF);
        addAction(Actions.EXPORT_FLAT_FILE);
        addAction(Actions.EXPORT_INSERTS);
    }
}
