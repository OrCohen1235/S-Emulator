package services;

import model.ProgramViewModel;
import java.util.List;

public interface ProgramStatsService {
    List<ProgramViewModel> fetchAllPrograms();
}