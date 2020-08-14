import java.util.*;

public class Main {
    public static void main(String[]args){
        AFD afd = null;
        AFN afn = null;

        Scanner input = new Scanner(System.in);
        int opcion = -1;
        System.out.println("Selecciona que deseas hacer\n2) Ingresar ER\n1) Ingresar AFN\n0) Ingresar AFD");
        do{
            String palabra = input.nextLine();
            try{
                opcion = Integer.parseInt(palabra);
            }catch (NumberFormatException ex){
                System.out.println("El input no es numerico");
                opcion = -1;
            }
            if(opcion<0 || opcion>2) System.out.println("Opción invalida");
        }while(opcion <0 || opcion>2);


        switch (opcion) {
            case(2):
                afn = ERinput();
                if(afn == null)return;

            case(1):
                if(afn == null) {
                    afn = AFNinput();
                    System.out.println("\n\nEl AFN ingresado es:\n" + afn);
                }
                else System.out.println("El AFN obtienido es:\n"+afn);
                afd = afn.getAFD();
            case(0):
                if(afd == null) {
                    afd = AFDinput();
                    System.out.println("El AFD ingresado es: \n" + afd+"\n");
                }
                else System.out.println("\nEl AFD equivalente es:\n"+afd);

                System.out.println("Ingresa palabras a evaluar. Terminar de ingresar palabras al ingresar la palabra compuesta por un espacio");;
                Scanner inputs = new Scanner(System.in);
                while (true) {
                    String word = inputs.nextLine();
                    if (word.equals(" ")) break;
                    System.out.println("La palabra: \""+word+"\" es "+((afd.resolve(word))?"Aprobada":"Rechazada")+" por el lenguaje");
                }
                AFD afdmin = afd.getMinimize();
                System.out.println("El AFD minimizado es: \n" + afdmin+"\n" +
                        "Ingresa palabras a evaluar. Terminar de ingresar palabras al ingresar la palabra compuesta por un espacio");
                while (true) {
                    String word = inputs.nextLine();
                    if (word.equals(" ")) break;
                    System.out.println("La palabra: \""+word+"\" es "+((afd.resolve(word))?"Aprobada":"Rechazada")+" por el lenguaje");
                }


        }
    }
    public static AFD AFDinput(){
        LinkedHashSet<String>estados = new LinkedHashSet<String>();
        LinkedHashSet<String>alfabeto = new LinkedHashSet<String>();
        LinkedHashSet<String>estadosFinales = new LinkedHashSet<String>();
        HashMap<ArrayList<String>,String> transiciones = new HashMap<ArrayList<String>,String>();
        Scanner inputs = new Scanner(System.in);
        String[] linea;
        String estado;

        System.out.println("Ingresa los estados existentes separados por espacios \" \"");
        linea = inputs.nextLine().split(" ");
        estados.addAll(Arrays.asList(linea));

        System.out.println("Ingresa el alfabeto separados por espacios \" \"\n" +
                "Nota: el alfabeto es exclusivamente caracteres");
        linea = inputs.nextLine().split(" ");
        for(String car:linea){
            while(car.length() != 1){
                System.out.println( "el caracter: "+car+" no tiene tamaño de 1,\n" +
                        "remplazalo por un caracter que cumpla con la condicion");
                car = inputs.nextLine();
            }
            alfabeto.add(car);
        }

        System.out.println("Ingresa los estados finales separados por espacios \" \"");
        linea = inputs.nextLine().split(" ");
        for(String est:linea){
            while (!estados.contains(est)){
                System.out.println( "el estado: "+est+" no se encuentra en los estados del automata,\n" +
                        "remplazalo por un estado correcto");
                est = inputs.nextLine();
            }
            estadosFinales.add(est);
        }
        System.out.println("Ingresa el estado inicial");
        estado = inputs.nextLine();
        while(!estados.contains(estado)){
            System.out.println( "el estado: "+estado+" no se encuentra en los estados del automata,\n" +
                    "remplazalo por un estado correcto");
            estado = inputs.nextLine();
        }
        String estadoInicial = estado;

        System.out.println("Ingresa la transicion para sus correspondientes valores:");
        String valor;
        for(String est:estados){
            for(String caracter:alfabeto){
                System.out.print("("+est+", "+caracter+") = ");
                valor = inputs.next();
                while(!estados.contains(valor)){
                    System.out.println("El estado resultante: \""+valor+"\" no se encuentra en los estados del automata,\n" +
                            "favor de remplazarlo por un estado correcto");
                    Scanner inputsError = new Scanner(System.in);
                    valor = inputsError.next();
                }
                ArrayList<String> par= new ArrayList<String>();
                par.add(est);
                par.add(caracter);
                transiciones.put(par,valor);
            }
        }
        System.out.println();
        System.out.println(estados);
        return new AFD(estados,alfabeto,transiciones,estadoInicial,estadosFinales);
    }

    public static AFN AFNinput(){
        LinkedHashSet<String>estados = new LinkedHashSet<String>();
        LinkedHashSet<String>alfabeto = new LinkedHashSet<String>();
        LinkedHashSet<String>estadosFinales = new LinkedHashSet<String>();
        HashMap<ArrayList<String>,HashSet<String>> transiciones = new  HashMap<ArrayList<String>,HashSet<String>>();
        Scanner inputs = new Scanner(System.in);
        String[] linea;
        String estado;

        System.out.println("Ingresa los estados existentes separados por espacios \" \"");
        linea = inputs.nextLine().split(" ");
        estados.addAll(Arrays.asList(linea));

        System.out.println("Ingresa el alfabeto separados por espacios \" \"\n" +
                "Nota: el alfabeto es exclusivamente caracteres");
        linea = inputs.nextLine().split(" ");
        for(String car:linea){
            while(car.length() != 1){
                System.out.println( "el caracter: "+car+" no tiene tamaño de 1,\n" +
                        "remplazalo por un caracter que cumpla con la condicion");
                car = inputs.nextLine();
            }
            alfabeto.add(car);
        }
        alfabeto.add("lambda");

        System.out.println("Ingresa los estados finales separados por espacios \" \"");
        linea = inputs.nextLine().split(" ");
        for(String est:linea){
            while (!estados.contains(est)){
                System.out.println( "el estado: "+est+" no se encuentra en los estados del automata,\n" +
                        "remplazalo por un estado correcto");
                est = inputs.nextLine();
            }
            estadosFinales.add(est);
        }
        System.out.println("Ingresa el estado inicial");
        estado = inputs.nextLine();
        while(!estados.contains(estado)){
            System.out.println( "el estado: "+estado+" no se encuentra en los estados del automata,\n" +
                    "remplazalo por un estado correcto");
            estado = inputs.nextLine();
        }
        String estadoInicial = estado;

        System.out.println("Ingresa las transicionones para sus correspondientes valores:\n " +
                "\tNota: Solo ingresa las transiciones colindantes, no es necesario hacer el trayecto de las lambdas.\n" +
                "\tSi no hay transición con algun simbolo, simplemente ingresar enter.");
        for(String est:estados){
            for(String caracter:alfabeto){

                System.out.print("("+est+", "+caracter+") = ");
                linea = inputs.nextLine().split(" ");
                HashSet<String> estadosLinea = new HashSet<>();
                for(String valor: linea){
                    if(estados.contains(valor)) estadosLinea.add(valor);
                }

                ArrayList<String> par= new ArrayList<String>();
                par.add(est);
                par.add(caracter);
                if(!estadosLinea.isEmpty())
                transiciones.put(par,estadosLinea);
            }
        }
        return new AFN(estados,alfabeto,transiciones,estadoInicial,estadosFinales);
    }
    private static AFN ERinput(){
        String s;
        Scanner inputs = new Scanner(System.in);

        System.out.println("Ingrese la expresión regular:");
        s = inputs.nextLine();

        return ExpresionRegular.interpretarExpresionRegular(s);
    }


}



