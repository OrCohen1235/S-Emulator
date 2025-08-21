package jaxbsprogram;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class ReadSemulatorXml {
    private SProgram simulator;
    private final File xmlFile;

    public ReadSemulatorXml(File file) {
        this.xmlFile = Optional.ofNullable(file)
                .orElseThrow(() -> new IllegalArgumentException("file is null"));

        Optional.of(xmlFile)
                .filter(f -> f.exists() && f.isFile())
                .orElseThrow(() -> new IllegalArgumentException("XML file not found: "
                        + xmlFile.getAbsolutePath() + "\n"));

        loadFiles();
    }


    private void loadFiles() {
        try {
            JAXBContext context = JAXBContext.newInstance(SProgram.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            this.simulator = (SProgram) unmarshaller.unmarshal(xmlFile);
        } catch (JAXBException e) {
            throw new RuntimeException("Failed to unmarshal XML from: " + xmlFile.getAbsolutePath() + "\n", e);
        }
    }

    public String getProgramName() {
        return simulator.getName();
    }

    public List<SInstruction> getSInstructionList() {
        return simulator.getSInstructions().getSInstruction();
    }

    public String checkLabelValidity() {
        List<SInstruction> list = getSInstructionList();

        return list.stream()
                .flatMap(inst -> Stream.ofNullable(inst.sInstructionArguments)
                                .flatMap(args -> args.getSInstructionArgument().stream()))
                .map(arg -> arg.value)
                .filter(this::checkIfLabel)
                .filter(lbl -> list.stream().noneMatch(i -> Objects.equals(i.sLabel, lbl)))
                .reduce((first, second) -> second)
                .orElse("");
    }

    private boolean checkIfLabel(String str) {
        return str.charAt(0) == 'L';
    }

}
