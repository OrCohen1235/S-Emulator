package semulator;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import java.io.File;
import java.util.List;

public class ReadSemulatorXml {
    private SProgram semulator;
    private File xmlFile;

    public ReadSemulatorXml(File file) {
        if (file == null) throw new IllegalArgumentException("file is null");
        if (!file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("XML file not found: " + file.getAbsolutePath());
        }
        this.xmlFile = file;
        loadFiles();
    }

    private void loadFiles() {
        try {
            JAXBContext context = JAXBContext.newInstance(SProgram.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            this.semulator = (SProgram) unmarshaller.unmarshal(xmlFile);
        } catch (JAXBException e) {
            throw new RuntimeException("Failed to unmarshal XML from: " + xmlFile.getAbsolutePath(), e);
        }
    }

    public SProgram getSemulator() {
        return semulator;
    }

    public String getProgramName() {
        return semulator.getName();
    }

    public List<SInstruction> getSInstructionList() {
        return semulator.getSInstructions().getSInstruction();
    }

}
