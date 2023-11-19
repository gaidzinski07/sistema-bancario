package entidade;

/* @author lucas 

Script de criação no Banco de dados:

CREATE TABLE IF NOT EXISTS `Agencia` (
  `id` int NOT NULL AUTO_INCREMENT,
  `endereco` varchar(40) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  PRIMARY KEY (`id`)
  ) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

 */
public class Agencia {

    private int id;
    private String endereco;

    public Agencia() {
    }

    public Agencia(int id, String endereco) {
        this.id = id;
        this.endereco = endereco;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public int getId() {
        return id;
    }

    public String getEndereco() {
        return endereco;
    }

}
