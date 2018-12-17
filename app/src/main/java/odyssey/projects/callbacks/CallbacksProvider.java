package odyssey.projects.callbacks;

public final class CallbacksProvider {

    private static MarkStatusListener   markStatusCallback   = null;
    private static MarksDatasetListener marksDatasetCallback = null;
    private static LoopsCountListener   loopsCountListener   = null;

    /* Изменение в текущем статусе отметки на удаленном сервере. */
    public static void registerMarkStatusListener(MarkStatusListener status) {markStatusCallback=status;}
    public static MarkStatusListener getMarkStatusCallback(){return markStatusCallback;}

    /* Изменения в наборе данных отметок в локальной БД. */
    public static void registerMarkDatasetListener(MarksDatasetListener dataset) {marksDatasetCallback=dataset;}
    public static MarksDatasetListener getMarksDatasetCallback(){return marksDatasetCallback;}

    /* Изменение в количестве пройденных кругов. */
    public static void registerLoopsListener(LoopsCountListener loops){loopsCountListener=loops;}
    public static LoopsCountListener getLoopsCountListener(){return loopsCountListener;}

}
