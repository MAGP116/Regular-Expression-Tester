

import java.util.*;

public class ExpresionRegular {

    //Clase hecha para manejar las operaciones a realizar del automata
    static protected class Token{
        protected enum States{
            EXRPRIM, OPENPARENT, CLOSEPARENT, UNION, CONCAT, ESTRELLA
        }
        States token;
        String car;
        public Token(States token, String car){
            this.token = token;
            this.car = car;
        }
        @Override
        public String toString(){
            return token+"\t"+car;
        }
    }

    //Clase Node hecha para manejar el arbol de operaciones
    static protected class Node{
        Token value;
        Node left;
        Node right;
        public Node(Token value){
            left = null;
            right = null;
            this.value = value;
        }
        @Override
        public String toString(){
            String s = "";

            s+=(this.value.car.equals("⋅"))?".":this.value.car;
            if(this.left != null)
                s+=this.left.toString();
            if(this.right != null)
                s+=this.right.toString();
            return s;
        }
    }

    //Recibe el string de la expresion regular y retorna un AFN que genera e lenguaje.
    public static AFN interpretarExpresionRegular(String expresion){
        LinkedList<Token> tokens = tokenizador(expresion);
        Node raiz = createBinaryTree(tokens);
        System.out.println("La expresión en notación polaca es: "+raiz);
        AFN afn = resolveNode(raiz);
        if(afn == null)System.out.println("La expresión ingresada no es una expresión regular");
        return afn;
    }


    //Recibe una expresion regular, y la convierte en una lista de tokens a realizar
    protected  static LinkedList<Token> tokenizador(String expresion){
        LinkedList<Token> tokens = new LinkedList<>();
        for(int i = 0; i<expresion.length(); i++){
            String car = String.valueOf(expresion.charAt(i));
            Token.States state;

            //Evalua caracter por caracter el tipo de token que es
            if(car.equals("("))state = Token.States.OPENPARENT;
            else if(car.equals(")"))state = Token.States.CLOSEPARENT;
            else if(car.equals("*"))state = Token.States.ESTRELLA;
            else if(car.equals("⋅"))state = Token.States.CONCAT;
            else if(car.equals("+"))state = Token.States.UNION;
            else if(car.equals(" ")) continue;
            else state = Token.States.EXRPRIM;
            Token previus = null;

            //añade los tokens de concatenación necesarios
            if(!tokens.isEmpty()){
                previus = tokens.getLast();
                if(previus.token == Token.States.ESTRELLA || previus.token == Token.States.CLOSEPARENT ||
                        previus.token == Token.States.EXRPRIM){
                    if(state == Token.States.EXRPRIM || state == Token.States.OPENPARENT)
                        tokens.add(new Token(Token.States.CONCAT,"⋅"));
                }
            }
                tokens.add(new Token(state,car));
        }
        return tokens;

    }

    //Recibe una lista de tokens, y lo convierte en un arbol de operaciones
    protected static Node createBinaryTree(LinkedList<Token> tokens){
        if(tokens == null || tokens.isEmpty())return null;
        //Encuentra la posicion de el token con menor relevancia correspondiente
        int pos = lessRelevant(tokens);
        if(pos == -1)return null;
        Node node = new Node(tokens.get(pos));

        LinkedList<Token> left = new LinkedList<>();
        LinkedList<Token> right;
        //Se dividen los tokens en dos, usando como corte el token con menor relevacia.
        tokens.remove(pos);
        for(int i = 0; i<pos; i++){
            left.add(tokens.poll());
        }
        right = tokens;
        //Se forman las ramas de la izquieda y derecha.
        node.left = createBinaryTree(left);
        node.right = createBinaryTree(right);
        return node;
    }

    //Metodo que determina la posicion que contiene el valor con menor relevancia.
    private static int lessRelevant(LinkedList<Token> tokens){
        if(tokens == null || tokens.isEmpty())return -1;
        int pos =  -1;
        int best = -1;
        int nivel = 0;
        for(int i = tokens.size()-1; i>=0;i--){
            Token token = tokens.get(i);
            //Se determina la profundidad a partir de los parentesis cerrados y abiertos.
            if(token.token == Token.States.CLOSEPARENT) {
                nivel--;
                continue;
            }
            else if(token.token == Token.States.OPENPARENT){
                nivel++;
                continue;
            }
            //Solo se evaluan los tokens si el nivel de profundidad es 0.
            if(nivel != 0)continue;

            int valor;
            //Se determina la relevancia del token
            switch (token.token){
                case UNION: valor = 3;break;
                case CONCAT: valor = 2;break;
                case ESTRELLA: valor = 1;break;
                default: valor = 0;
            }
            //Si es menos relevante se ingresa
            if(valor>best){
                best = valor;
                pos = i;
            }
            //Si es el menos relevante, no es necesario seguir buscando
            if(valor == 3) break;
        }
        //Si se buscaron en todos los Tokens y no se encontró un token con relevancia, entonces hay que corregir los
        //Parentesis
        if(pos == -1){
            repairParentesis(tokens);
            return lessRelevant(tokens);
        }
        return pos;
    }

    //Metodo que se encarga de reparar parentesis no simetricos o redundantes
    protected static void repairParentesis(LinkedList<Token> tokens){
        int nivel = 0;
        int firstOpen = -1;
        int lastClose = tokens.size()+1;
        int pos = 0;
        //Evalua la simetria
        for(Token token : tokens){
            if(token.token == Token.States.CLOSEPARENT) {
                nivel--;
                lastClose = pos;
            }
            else if(token.token == Token.States.OPENPARENT){
                nivel++;
                if(firstOpen == -1)firstOpen = pos;
            }
            pos++;
        }
        //Arregla segun la simetria
        if(nivel<0)tokens.remove(lastClose);
        else if(nivel>0)tokens.remove(firstOpen);
        //Arregla parentesis redundantes
        else if(lastClose != tokens.size()+1 && firstOpen != 1){
            tokens.remove(lastClose);
            tokens.remove(firstOpen);
        }
    }

    //metodo que se encarga de resolver un nodo.
    protected static AFN resolveNode(Node node){
        //Si se proviene de un nodo sin hijos se regresa null
        if(node == null)return null;
        AFN left, right;
        //llama a resolver sus hijos
        left = resolveNode(node.left);
        right = resolveNode(node.right);
        //Determina el tipo de token y se resuelve a partir de sus hijos.
        switch (node.value.token){
            case EXRPRIM:
                return AFN.expresionRegularPrimitiva(node.value.car);
            case ESTRELLA:
                if(left == null)return null;
                return AFN.extenderEstrella(left);
            case CONCAT:
                if(left == null || right == null)return null;
                return AFN.concatenar(left,right);
            case UNION:
                if(left == null || right == null)return null;
                return AFN.union(left,right);

        }
        return null;
    }
}

