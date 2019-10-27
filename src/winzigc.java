
/*
 *
 *          compile : javac winzigc.java
 *              run : java Program
 *  compile and run : javac winzigc.java; java winzigc –ast winzig_test_programs/winzig_01 > tree.01
 *
 */

public class winzigc {
    public static void main(String[] args) {
        String flag = args[0];
        switch (flag){
            case  "-ast":
                String path_to_winzig_program = args[1];
                System.out.println("generate AST for "+ path_to_winzig_program);
                // call lexer and parser
                break;
            case "-codegen":
                break;
            case  "-h":
                System.out.println("run command: java winzigc [stage] [path]");
                System.out.println("    [stage]: specifies where to stop in the steps of winzigc compiler");
                System.out.println("            -ast: generate 'Abstract Syntax Tree' from the winzig program found using file path given by [path]");
                System.out.println("     [path]: relative path to the winzig program");
                break;
            default:
                System.out.println("Provided args are incompatible. Run with flag -h for help");
                System.out.println("java winzigc -h");
        }
    }
}

class Lexer{

}





