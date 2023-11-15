package entidade;

public class Contasusuario {

    private int id;
    private int id_user;
    private int tipoconta;
    private int agencia;
    private int numconta;
    private float saldo;

    public Contasusuario() {
        this.id = 0;
        this.id_user=0;
        this.tipoconta=0;
        this.agencia=0;
        this.numconta=0;
        this.saldo=0;
    }

    public Contasusuario(int id_user, int tipoconta, int agencia, int numconta, float saldo) {
        //this.id = id;
        //this.id = id;
        this.id_user=id_user;
        this.tipoconta=tipoconta;
        this.agencia=agencia;
        this.numconta=numconta;
        this.saldo=saldo;
        
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId_user() {
        return id_user;
    }

    public void setId_user(int id_user) {
        this.id_user = id_user;
    }
    
    public int gettipoconta() {
        return tipoconta;
    }

    public void settipoconta(int tipoconta) {
        this.tipoconta = tipoconta;
    }
    
    public int getagencia() {
        return agencia;
    }

    public void setagencia(int agencia) {
        this.agencia = agencia;
    }
    
    public int getnumconta() {
        return numconta;
    }

    public void setnumconta(int numconta) {
        this.numconta = numconta;
    }
    
    public float getsaldo() {
        return saldo;
    }

    public void setsaldo(float saldo) {
        this.saldo = saldo;
    }
}
