package odyssey.projects.callbacks;

import odyssey.projects.services.MarkOpService;

public interface MarkStatusListener {
    public void changed(MarkOpService.StatusEnum newStatus);
}
