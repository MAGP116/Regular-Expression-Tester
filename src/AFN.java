import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class AFN extends AutomataFinito {
    protected Map<ArrayList<String>, HashSet<String>> transiciones;

    public AFN(Set<String> estados, Set<String>alfabeto, Map<ArrayList<String>,HashSet<String>> transiciones, String estadoInicial, Set<String>estadosFinales){
        super(estados,alfabeto,estadoInicial,estadosFinales);
        this.transiciones = transiciones;
    }
    protected String getTransitionFunction(){
        String string = "";
        for (String estado:estados) {
            for (String caracter: alfabeto) {
                String derecha;
                HashSet<String> transicion =this.transiciones.get(Par(estado,caracter));
                if(transicion != null) {
                    derecha = new TreeSet<>(transicion).toString();
                    string += "\n(" + estado + ", " + caracter + ") = " + derecha;
                }
            }

        }
        return string;
    }

    //La resolución de lambdas transforma un AFN con simbolo de lambda a un AFN que contiene ya interpretadas todas las
    //transiciones que se puede obtener con las lambdas.
    public void resolveLambdas(){
        if(!alfabeto.contains("lambda"))return;
        for(String est: estados){
            for(String car: alfabeto){
                //Se evaluan los estados que se obtienen con la transicion de estado caracter
                HashSet<String> transicion = transiciones.get(Par(est,car));
                if(transicion == null)continue;
                ConcurrentLinkedQueue<String> estadosAlcanzables = new ConcurrentLinkedQueue<>(transicion);
                HashSet<String> nuevosEstados = new HashSet<>();
                //Toda transicion de un estado alcanzable con una lambda se añade a los estados de la transicion
                while(!estadosAlcanzables.isEmpty()){
                    String estadoActual = estadosAlcanzables.poll();
                    nuevosEstados.add(estadoActual);
                    HashSet<String> estadosTransitables = transiciones.get(Par(estadoActual,"lambda"));
                    if(estadosTransitables == null)continue;
                    for(String estTrans: estadosTransitables){
                        if(!nuevosEstados.contains(estTrans)) {
                            estadosAlcanzables.add(estTrans);
                            nuevosEstados.add(estTrans);
                        }
                    }
                }
                transiciones.put(Par(est,car),nuevosEstados);
            }

            //se evaluan todas las transiciones de estado con lambda con todas los simbolos para añadirlos
            HashSet<String> transicionesLambda = transiciones.get(Par(est,"lambda"));
            if(transicionesLambda == null)continue;
            for(String car : alfabeto){
                if(car.equals("lambda"))continue;
                //se obtienen las transiciones actuales del estado
                HashSet<String> transicionActual = transiciones.get(Par(est,car));
                if(transicionActual == null)transicionActual = new HashSet<>();
                //se añaden las transiciones por cada caracter
                for(String estTrans : transicionesLambda){
                    HashSet<String> estadosTransitados =transiciones.get(Par(estTrans,car));
                    if(estadosTransitados == null)continue;
                    for(String trans :estadosTransitados) {
                        if(estados.contains(trans))
                            transicionActual.add(trans);
                    }
                }
                if(!transicionActual.isEmpty())
                transiciones.put(Par(est,car),transicionActual);
            }
        }
        for(String est: estados){
            HashSet<String> estadosTransitables = transiciones.get(Par(est,"lambda"));
            if(estadosTransitables == null)continue;
            for(String estado : estadosTransitables){
                if(this.estadosFinales.contains(estado))this.estadosFinales.add(est);
            }
        }
        for(String est : estados){
            if(transiciones.get(Par(est,"lambda")) != null)
                transiciones.remove(Par(est,"lambda"));
        }
        alfabeto.remove("lambda");
    }

    private static ArrayList<String> Par(String first, String second){
        ArrayList<String> par= new ArrayList<String>();
        par.add(first);
        par.add(second);
        return par;
    }

    public AFD getAFD(){
        resolveLambdas();

       HashMap<ArrayList<String>,String> nuevoTransiciones = new HashMap<>();
       LinkedHashSet<String> nuevoEstadosFinales = new LinkedHashSet<>();
       LinkedHashSet<String> nuevoAlfabeto = new LinkedHashSet<>(this.alfabeto);
       String nuevoEstadoInicial = "0";

       if(this.estadosFinales.contains(this.estadoInicial))nuevoEstadosFinales.add("0");

        LinkedList<HashSet<String>> estadosEncontrados = new LinkedList<>();
        ConcurrentLinkedQueue<HashSet<String>> estadosaEvaluar = new ConcurrentLinkedQueue<>();
        HashSet<String> estadosActuales = new HashSet<>();

        estadosActuales.add(this.estadoInicial);
        estadosaEvaluar.add(estadosActuales);
        estadosEncontrados.add(estadosActuales);

        while(!estadosaEvaluar.isEmpty()){
            estadosActuales = estadosaEvaluar.poll();
            for(String car : this.alfabeto){
                HashSet<String> transicion = new HashSet<>();
                for(String est: estadosActuales){
                    HashSet<String> tran = this.transiciones.get(Par(est,car));
                    if(tran != null)
                    transicion.addAll(tran);

                }
                if(!estadosEncontrados.contains(transicion)){
                    estadosEncontrados.add(transicion);
                    estadosaEvaluar.offer(transicion);
                }
                nuevoTransiciones.put(Par(String.valueOf(estadosEncontrados.indexOf(estadosActuales)),car),String.valueOf(estadosEncontrados.indexOf(transicion)));
                for(String est: transicion){
                    if(this.estadosFinales.contains(est)){
                        nuevoEstadosFinales.add(String.valueOf(estadosEncontrados.indexOf(transicion)));
                        break;
                    }
                }
            }

        }

        LinkedHashSet<String> nuevoEstados = new LinkedHashSet<>();
        for(int i = 0; i<estadosEncontrados.size(); i++){
            nuevoEstados.add(String.valueOf(i));
        }

        return new AFD(nuevoEstados,nuevoAlfabeto,nuevoTransiciones,nuevoEstadoInicial,nuevoEstadosFinales);
    }

    //concatena el afn a con el afn b, conectando de a a b
    public static AFN concatenar(AFN a, AFN b){
        if(a.estadosFinales.size() != 1 || b.estadosFinales.size() != 1)return null;

        b.alfabeto.add("lambda");
        a.alfabeto.addAll(b.alfabeto);

        LinkedHashSet<String> nuevoEstadosFinales = new LinkedHashSet<>();
        int desp = a.estados.size();
        a.unificate(b);
        HashSet<String> transicion;
        transicion = a.transiciones.get(Par(a.estadosFinales.iterator().next(),"lambda"));
        if(transicion == null) transicion = new HashSet<>();
        transicion.add(String.valueOf(Integer.parseInt(b.estadoInicial)+desp));
        a.transiciones.put(Par(a.estadosFinales.iterator().next(),"lambda"),transicion);
        nuevoEstadosFinales.add(String.valueOf(Integer.parseInt(b.estadosFinales.iterator().next())+desp));
        a.estadosFinales = nuevoEstadosFinales;
        return a;
    }
    //Genera un AFN sin transiciones y con un solo estado 0.
    public static AFN getEmptyAFN(){
        LinkedHashSet<String> alfabeto = new LinkedHashSet<>();
        LinkedHashSet<String> estados = new LinkedHashSet<>();
        LinkedHashSet<String> estadosFinales = new LinkedHashSet<>();
        HashMap<ArrayList<String>,HashSet<String>> transiciones = new  HashMap<>();
        String estadoInicial = "0";
        estados.add("0");
        estadosFinales.add("0");
        alfabeto.add("lambda");
        return new AFN(estados,alfabeto,transiciones,estadoInicial,estadosFinales);
    }
    //Retorna el AFN resultante de extender a estrella el afn ingresado.
    public static AFN extenderEstrella(AFN afn){

        //se conecta el final con el inicio
        HashSet<String> transicion = afn.transiciones.get(Par(afn.estadosFinales.iterator().next(),"lambda"));
        if(transicion == null)transicion = new HashSet<>();
        transicion.add(afn.estadoInicial);
        afn.transiciones.put(Par(afn.estadosFinales.iterator().next(),"lambda"),transicion);

        //se conecta el inicio con el final
        transicion = afn.transiciones.get(Par(afn.estadoInicial,"lambda"));
        if(transicion == null)transicion = new HashSet<>();
        transicion.add(afn.estadosFinales.iterator().next());
        afn.transiciones.put(Par(afn.estadoInicial,"lambda"),transicion);
        return afn;
    }
    //Entrega un AFN producto de hacer la union del AFN a y el b.
    public static AFN union(AFN a, AFN b){
        if(a == null || b == null)return null;
        if(a.estadosFinales.size() != 1 || b.estadosFinales.size() != 1)return null;
        AFN start = getEmptyAFN();
        start.alfabeto.addAll(a.alfabeto);
        start.alfabeto.addAll(b.alfabeto);

        LinkedHashSet<String> nuevoEstadosFinales = new LinkedHashSet<>();
        int desp = start.estados.size();
        start.unificate(a);

        //conecta el inicio con el estado inicial de a
        HashSet<String> transicion = new HashSet<>();
        transicion.add(String.valueOf(Integer.parseInt(a.estadoInicial)+desp));
        start.transiciones.put(Par(start.estadoInicial,"lambda"),transicion);

        //Se crea el nuevo estado final
        String estadoFinal = String.valueOf(start.estados.size());
        start.estados.add(estadoFinal);
        nuevoEstadosFinales.add(estadoFinal);
        //se conecta el estado final de a con el estado final nuevo
        transicion = start.transiciones.get(Par(String.valueOf(Integer.parseInt(a.estadosFinales.iterator().next())+desp),"lambda"));
        if(transicion == null)transicion = new HashSet<>();
        transicion.add(estadoFinal);
        start.transiciones.put(Par(String.valueOf(Integer.parseInt(a.estadosFinales.iterator().next())+desp),"lambda"),transicion);

        desp = start.estados.size();
        start.unificate(b);
        //se conecta el estado inicial con el estado inicial de b
        transicion = start.transiciones.get(Par(start.estadoInicial,"lambda"));
        transicion.add(String.valueOf(Integer.parseInt(b.estadoInicial)+desp));
        start.transiciones.put(Par(start.estadoInicial,"lambda"),transicion);

        //se conecta el estado final de b con el estado final
        transicion = start.transiciones.get(Par(String.valueOf(Integer.parseInt(b.estadosFinales.iterator().next())+desp),"lambda"));
        if(transicion == null)transicion = new HashSet<>();
        transicion.add(estadoFinal);
        start.transiciones.put(Par(String.valueOf(Integer.parseInt(b.estadosFinales.iterator().next())+desp),"lambda"),transicion);
        start.estadosFinales = nuevoEstadosFinales;
        return start;

    }

    //Elabora la expresion regular primitiva
    public static AFN expresionRegularPrimitiva(String car){
        LinkedHashSet<String> alfabeto = new LinkedHashSet<>();
        LinkedHashSet<String> estados = new LinkedHashSet<>();
        LinkedHashSet<String> estadosFinales = new LinkedHashSet<>();
        HashMap<ArrayList<String>,HashSet<String>> transiciones = new  HashMap<ArrayList<String>,HashSet<String>>();
        String estadoInicial = "0";
        estados.add("0");
        estados.add("1");
        alfabeto.add(car);
        alfabeto.add("lambda");
        estadosFinales.add("1");
        HashSet<String> transicion = new HashSet<>();
        transicion.add("1");
        transiciones.put(Par("0",car),transicion);
        return new AFN(estados,alfabeto,transiciones,estadoInicial,estadosFinales);
    }

    //Añade a el AFN objeto, todos los estados del AFN ingresado como argumento, junto con su tabla de transicion, para
    //esto, desplaza los estados del argumento a uno que no haya sido usado aun, de la misma forma, se desplazan
    //las transiciones del afn.
    private void unificate(AFN afn){
        int desp = this.estados.size();
        for(String est: afn.estados){
            for(String car: afn.alfabeto){
                HashSet<String> nuevaTransicion = new HashSet<>();
                HashSet<String> transicion = afn.transiciones.get(Par(est,car));
                if(transicion != null && !transicion.isEmpty())  {
                    for (String estado : transicion)
                        nuevaTransicion.add(String.valueOf(Integer.parseInt(estado) + desp));
                    this.transiciones.put(Par(String.valueOf(Integer.parseInt(est) + desp), car), nuevaTransicion);

                }
            }
        }
        for(String est: afn.estados){
            this.estados.add(String.valueOf(Integer.parseInt(est)+desp));
        }

    }
}












