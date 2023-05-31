/**
 * Clase que modela al usuario de la sesion
 */
public class Usuario {
    private String username; // el nombre de usuario

    /**
     * Obtiene el nombre del usuario
     * @return el nombre de usuario
     */
    public String getUsername() {
        return username;
    }

    /**
     * Configura el nombre del usuario actual
     * @param username el nombre de usuario
     */
    public void setUsername(String username) {
        this.username = username;
    }
}
