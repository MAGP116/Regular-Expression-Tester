import java.util.Set;


public abstract class AutomataFinito {
    protected Set<String> estados;
    protected Set<String> alfabeto;
    protected Set<String> estadosFinales;
    protected String estadoInicial;

    public AutomataFinito(Set<String>estados,Set<String>alfabeto, String estadoInicial,Set<String>estadosFinales){
        this.estados = estados;
        this.estadosFinales = estadosFinales;
        this.alfabeto = alfabeto;
        this.estadoInicial = estadoInicial;
    }
    //Método a implementar que retorna el String que contiene la tabla de transicones del automata
    protected abstract String getTransitionFunction();

    @Override
    public String toString(){

        String string = "Estados: "+this.estados+"\n" +
                "Alfabeto: "+this.alfabeto+"\n" +
                "Estado Inicial: "+this.estadoInicial+"\n" +
                "Estados Finales: "+this.estadosFinales+"\n" +
                "Funciones de transicion:"+getTransitionFunction();
        return string;
    }


}
