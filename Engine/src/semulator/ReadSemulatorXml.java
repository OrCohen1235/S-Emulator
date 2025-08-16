package semulator;

import Logic.Instruction;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import semulator.SProgram;

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

    //    public static void checkFiles(SProgram program) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
//       Class clazz = program.getClass();
//       Method[] methods = clazz.getDeclaredMethods();
//       Method m = clazz.getMethod("getSInstructions");
//       Object o = m.invoke(program);
//       Class clazz1 = o.getClass();
//       Method checkList = clazz1.getMethod("getSInstruction");
//       List<Object> o2 = (List)checkList.invoke(o);
//       System.out.println(o2);
//
//       Class clazz2 = o2.get(0).getClass();
//       System.out.println(clazz2.getName());
//       Method checkList2 = clazz2.getMethod("getName");
//       Method checkList3 = clazz2.getMethod("get");
//       Object o3 = checkList2.invoke(o2.get(0));
//       System.out.println(o3);
//    }
}
