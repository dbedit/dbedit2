package dbedit.actions;

public class LobGroupAction extends GroupAction {

    protected LobGroupAction() {
        super("Lob");
        addAction(Actions.LOB_IMPORT);
        addAction(Actions.LOB_EXPORT);
        addAction(Actions.LOB_COPY);
        addAction(Actions.LOB_PASTE);
    }
}
