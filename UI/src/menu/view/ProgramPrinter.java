package menu.view;

import logic.dto.ProgramDTO;

import java.util.List;

public class ProgramPrinter {
    public void printProgram(ProgramDTO dto, List<String> expandedCommands) {
        System.out.println("\nProgramName: \n" + dto.getProgramName());

        System.out.println("\nVariables: ");
        dto.getVariables().forEach(System.out::println);

        System.out.println("\nLabels: ");
        dto.getLabels().forEach(System.out::println);

        System.out.println("\nCommands: ");
        for (String line : expandedCommands) System.out.println(line);
    }
}
