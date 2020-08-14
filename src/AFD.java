import java.util.*;

import java.util.concurrent.ConcurrentLinkedQueue;


public class AFD extends AutomataFinito {
    protected Map<ArrayList<String>,String> transiciones;

    public AFD(Set<String> estados, Set<String>alfabeto,Map<ArrayList<String>,String> transiciones, String estadoInicial, Set<String>estadosFinales){
        super(estados,alfabeto,estadoInicial,estadosFinales);
        this.transiciones = transiciones;
    }

    protected String getTransitionFunction(){
        String string = "";
        for (String estado:this.estados) {
            for (String caracter: this.alfabeto) {
                ArrayList<String> par= new ArrayList<String>();
                par.add(estado);
                par.add(caracter);
                string += "\n("+estado+", "+caracter+") = "+this.transiciones.get(par);
            }

        }
        return string;
    }
    public boolean resolve(String word){
        String actEst = this.estadoInicial;
        for(int i = 0; i<word.length(); i++){
            String car =word.substring(i,i+1);
            if(!this.alfabeto.contains(car))return false;
            ArrayList<String> par = new ArrayList<String>();
            par.add(actEst);
            par.add(car);
            actEst = this.transiciones.get(par);
        }
        if(this.estadosFinales.contains(actEst)) return true;
        return false;
    }

    //metodo que retorna un AFD equivalente minimizado.
    public AFD getMinimize(){
        HashSet<String> estadosNoFinales = new HashSet<String>();
        HashSet<String> estadosFinales = new HashSet<String>();
        ConcurrentLinkedQueue<String> buscadorEstados = new ConcurrentLinkedQueue<String>();
        buscadorEstados.add(this.estadoInicial);

        //determina los estados accesibles y los almacena en su correspondiente conjunto.
        while(!buscadorEstados.isEmpty()){
            String est = buscadorEstados.remove();
            if(estadosNoFinales.contains(est) == estadosFinales.contains(est)){
                if(this.estadosFinales.contains(est))estadosFinales.add(est);
                else estadosNoFinales.add(est);
                for(String car: this.alfabeto) {
                    ArrayList<String> par = new ArrayList<String>();
                    par.add(est);
                    par.add(car);
                    buscadorEstados.add((this.transiciones.get(par)));
                }
            }
        }


        LinkedHashSet<String>nuevoEstadosFinales = new LinkedHashSet<String>();
        //Se crea una lista que almacenara los estados distingibles, y se almacenan los dos estados obtenidos hasta el momento.
        LinkedList<HashSet<String>> estadosDiferenciables = new LinkedList<HashSet<String>>();
        if(!estadosFinales.isEmpty()) {
            estadosDiferenciables.addLast((estadosFinales));
            //Crea un conjunto que almacena los estados finales y asigna el conjunto en la posicion 1 como un conjunto con estado final.
            nuevoEstadosFinales.add("0");
        }
        if(!estadosNoFinales.isEmpty()) estadosDiferenciables.addLast(estadosNoFinales);


        boolean cambio;
        //Se revisan los estados Diferenciables hasta que se complete un ciclo sin producir un cambio.
        do{
            cambio = false;
            //por cada conjunto en los estados diferenciables
            for (int i = 0; i< estadosDiferenciables.size(); i++){
                HashSet<String> conjunto = estadosDiferenciables.get(i);
                LinkedList<String> valoresDelConjunto = new LinkedList<String>();
                HashSet<String> nuevoConjunto = new HashSet<String>();
                //se evaluan sus estados internos del conjunto
                for(String estado:conjunto){
                    if(valoresDelConjunto.isEmpty()){
                        valoresDelConjunto = valuesOfState(estado, estadosDiferenciables);
                        continue;
                    }
                    LinkedList<String> valoresDelEstado = valuesOfState(estado, estadosDiferenciables);
                    //Se comparan las transiciones del primer valor con las de el resto del conjunto, si son diferentes
                    //se trasladaran a un conjunto nuevo
                    if(!valoresDelConjunto.equals(valoresDelEstado)) nuevoConjunto.add(estado);


                }
                //Sí se encontraron estados diferenciables en el mismo conjunto, se guardara este conjunto nuevo
                if(!nuevoConjunto.isEmpty()){
                    estadosDiferenciables.addLast(nuevoConjunto);
                    //se eliminan del viejo conjunto los estados distingibles
                    for(String est: nuevoConjunto)estadosDiferenciables.get(i).remove(est);
                    cambio = true;
                    //se determina si el nuevo conjunto es un estado final.
                    if(nuevoEstadosFinales.contains(String.valueOf(i)))nuevoEstadosFinales.add(String.valueOf(estadosDiferenciables.size()-1));
                    break;
                }
            }
        }while(cambio);

        // se almacena el alfabeto.
        LinkedHashSet<String>nuevoAlfabeto = new LinkedHashSet<String>(alfabeto);
        String nuevoEstadoInicial = "";
        LinkedHashSet<String>nuevoEstados = new LinkedHashSet<>();
        HashMap<ArrayList<String>,String> nuevoTransiciones = new HashMap<ArrayList<String>,String>();

        for(int i = 0; i<estadosDiferenciables.size(); i++){
            //Se guardan los nuevos estados del automata
            nuevoEstados.add(String.valueOf(i));
            //se determina el nuevo estado inicial del automata
            if(estadosDiferenciables.get(i).contains(this.estadoInicial))
                nuevoEstadoInicial = String.valueOf(i);
        }

        //Se determinan las nuevas transiciones del automata
        int i = 0;
        for(String est: nuevoEstados){
            int j = 0;
            HashSet<String> estados = estadosDiferenciables.get(i);
            if(estados == null || estados.isEmpty()){
                i++;
                continue;
            }
            String valor = estados.iterator().next();
            LinkedList<String> valoresDelConjunto = valuesOfState(valor, estadosDiferenciables);
            for(String car: nuevoAlfabeto){
                ArrayList<String> par = new ArrayList<String>();
                par.add(est);
                par.add(car);
                nuevoTransiciones.put(par,valoresDelConjunto.get(j));
                j++;
            }
            i++;
        }

        return new AFD(nuevoEstados,nuevoAlfabeto,nuevoTransiciones,nuevoEstadoInicial,nuevoEstadosFinales);
    }

    private LinkedList<String> valuesOfState(String estado,LinkedList<HashSet<String>> estadosDiferenciables) {
        LinkedList<String> valoresDelEstado = new LinkedList<String>();
        for (String car : this.alfabeto) {
            ArrayList<String> par = new ArrayList<>();
            par.add(estado);
            par.add(car);
            String transicion = (this.transiciones.get(par));
            for (int i = 0; i < estadosDiferenciables.size(); i++) {
                if (estadosDiferenciables.get(i).contains(transicion))
                    valoresDelEstado.add(String.valueOf(i));
            }
        }
        return valoresDelEstado;
    }

}
