// dto/ConnectedUserDTO.java
package logic.dto;

public class ConnectedUserDTO {
    public String name;
    public int mainPrograms;
    public int functions;
    public int creditsCurrent;
    public int creditsUsed;
    public int runs;

    public ConnectedUserDTO(String name, int mainPrograms, int functions,
                            int creditsCurrent, int creditsUsed, int runs) {
        this.name = name;
        this.mainPrograms = mainPrograms;
        this.functions = functions;
        this.creditsCurrent = creditsCurrent;
        this.creditsUsed = creditsUsed;
        this.runs = runs;
    }
}
