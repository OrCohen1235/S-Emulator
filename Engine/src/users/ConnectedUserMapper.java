// web-demo/src/main/java/dto/ConnectedUserMapper.java
package users;

import logic.dto.ConnectedUserDTO;
import users.User;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ConnectedUserMapper {
    public static List<ConnectedUserDTO> fromUsers(Collection<User> users) {
        return users.stream().map(u ->
                new ConnectedUserDTO(
                        u.getUsername(),
                        u.getMainProgramsUploaded(),
                        u.getFunctionsContributed(),
                        u.getCreditsCurrent(),
                        u.getCreditsUsed(),
                        u.getRunsCount()
                )
        ).collect(Collectors.toList());
    }
}
