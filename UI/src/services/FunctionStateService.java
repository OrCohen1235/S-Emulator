package services;

import model.FunctionViewModel;
import model.ProgramViewModel;

import java.util.List;

public interface FunctionStateService {
    List<FunctionViewModel> fetchAllFunction();

}