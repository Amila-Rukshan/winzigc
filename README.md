# winzigc
CS4542 - Compiler Design : project to build a c- type compiler 

## Token Definitions

### Definitions.
* **Identifier**: Any sequence of characters which may contain alphabets, digits from
0 to 9 or an underscore. The sequence must start with an alphabet or an
underscore.
* **Integer**: A sequence of characters which may contain any combination of digits
from 0 to 9.
* White Space: Any sequence of characters containing any combination of single
space, form feed, horizontal tab and vertical tab.
* **Char**: A sequence of three characters which contains a single character between
two single quotes. The single character between the two quotes can be any
character except a single quote itself. For example, ‘’’ is an invalid character.
* **String**: A sequence of characters which contains any number of characters
between two double quotes. The character sequence between the two double
quotes can be any character except a double quote itself. For example,
“Hello”how” is an invalid string.
* **Comment**: Type 1: a sequence of characters which starts with # and ends with a
newline. Type2: begins with ‘{‘, continues with any character (including end-of-
line), and ends with ‘}’.

### Pre-defined tokens in the language are given below:

| No. | Token         | Description                         |
| --- |-------------- | ----------------------------------- |
|   1 |**\n**         | Newline  |
|   2 |**program**         | Start of Program  |
|   3 |**var**         | Variable  |
|   4 |**const**         | Constant  |
|   5 |**type**         | To define a data type  |
|   6 |**function**         | To define a function  |
|   7 |**return**         | return from function  |
|   8 |**begin**         | start of a block  |
|   9 |**end**         | end of a block  |
|  10 |**:=:**         | swap |
|  11 |**:=**         | assignment operator |
|  12 |**output**         | output an expression or string |
|  13 |**if**         | keyword |
|  14 |**then**         | keyword |
|  15 |**else**         | keyword |
|  16 |**while**         | keyword for loop |
|  17 |**do**         | keyword for loop |
|  18 |**case**         | keyword |
|  19 |**of**         | keyword |
|  20 |**..**         | dots for case expression |
|  21 |**otherwise**         | keyword |
|  22 |**repeat**         | keyword for repeat-until loop |
|  23 |**for**         | keyword for loop |
|  24 |**until**         | keyword for repeat-until loop |
|  25 |**loop**         | keyword for loop-pool loop |
|  26 |**pool**         | keyword for loop-pool loop |
|  27 |**exit**         | keyword |
|  28 |**<=**         | less than equal to binary operator |
|  29 |**<>**         | not equal to binary operator |
|  30 |**<**         | less than binary operator |
|  31 |**>=**         | greater than equal to binary operator |
|  32 |**>**         | greater than binary operator |
|  33 |**=**         | equal to binary operator |
|  34 |**mod**         | modulus binary operator |
|  35 |**and**         | and binary operator |
|  36 |**or**         | or binary operator |
|  37 |**not**         | not unary operator |
|  38 |**read**         | read an identifier |
|  39 |**succ**         | successor of an ordinal value |
|  40 |**pred**         | predecessor of a ordinal value |
|  41 |**chr**         | keyword for character function |
|  42 |**ord**         | keyword for ordinal function |
|  43 |**eof**         | keyword for end of file |
|  44 |**{**         | begin of a block |
|  45 |**:**         | colon |
|  46 |**;**         | semi colon |
|  47 |**.**         | single dot |
|  48 |**,**         | comma |
|  49 |**(**         | opening bracket |
|  50 |**)**         | closing bracket |
|  51 |**+**         | plus |
|  52 |**-**         | minus |
|  53 |*         | multiply |
|  54 |**/**         | divide |

### Grammar rules for WinZig

<pre>
Winzig     ->  'program' Name ':' Consts Types Dclns SubProgs Body Name '.'  => "program";

Consts     ->  'const' Const list ',' ';'                                    => "consts"
           ->                                                                => "consts";
 
Const      ->  Name '=' ConstValue                                           => "const"; 

ConstValue ->  '<integer>'
           ->  '<char>'
           ->  Name;

Types      ->  'type' (Type ';')+                                            => "types"
           ->                                                                => "types";
           
Type       ->  Name '=' LitList                                              => "type";

LitList    ->  '(' Name list ',' ')'                                         => "lit";

SubProgs   ->  Fcn*                                                          => "subprogs";

Fcn        ->  'function' Name '(' Params ')' ':' Name                       
               ';' Consts Types Dclns Body Name ';'                          => "fcn";

Params     ->  Dcln list ';'                                                 => "params";

Dclns      ->  'var' (Dcln ';')+                                             => "dclns"
           ->                                                                => "dclns";

Dcln       ->  Name list ',' ':' Name                                        => "var";

Body       ->  'begin' Statement list ';' 'end'                              => "block";
 
Statement  ->  Assignment
           ->  'output' '(' OutExp list ',' ')'                              => "output"
           ->  'if' Expression 'then' Statement ('else' Statement)?          => "if"
           ->  'while' Expression 'do' Statement                             => "while"
           ->  'repeat' Statement list ';' 'until' Expression                => "repeat"
           ->  'for' '(' ForStat ';' ForExp ';' ForStat ')' Statement        => "for"
           ->  'loop' Statement list ';' 'pool'                              => "loop"
           ->  'case' Expression 'of' Caseclauses OtherwiseClause 'end'      => "case"
           ->  'read' '(' Name list ',' ')'                                  => "read"
           ->  'exit'                                                        => "exit"
           ->  'return' Expression                                           => "return"
           ->  Body
           ->                                                                => "<null>";
  
OutExp     ->  Expression                                                    => "integer"
           ->  StringNode                                                    => "string";
          
StringNode ->  '<string>';
  
Caseclauses->  (Caseclause ';')+;
 
Caseclause ->  CaseExpression list ',' ':' Statement => "case_clause";

CaseExpression 
           ->  ConstValue
           ->  ConstValue '..' ConstValue                                    => "..";
           
OtherwiseClause
           ->  'otherwise' Statement                                         => "otherwise"
           ->  ; 
           
Assignment ->  Name ':=' Expression                                          => "assign"
           ->  Name ':=:' Name                                               => "swap";                                          
ForStat    ->  Assignment
           ->                                                                => "<null>";
  
ForExp     ->  Expression
           ->                                                                => "true";

Expression ->  Term
           ->  Term '<=' Term                                                => "<="
           ->  Term '<' Term                                                 => "<"
           ->  Term '>=' Term                                                => ">="
           ->  Term '>' Term                                                 => ">"
           ->  Term '=' Term                                                 => "="
           ->  Term '<>' Term                                                => "<>";

Term       ->  Factor
           ->  Term '+' Factor                                               => "+"
           ->  Term '-' Factor                                               => "-"
           ->  Term 'or' Factor                                              => "or";
           
Factor     ->  Factor '*' Primary                                            => "*"
           ->  Factor '/' Primary                                            => "/"
           ->  Factor 'and' Primary                                          => "and"
           ->  Factor 'mod' Primary                                          => "mod"
           ->  Primary;  
  
Primary    ->  '-' Primary                                                   => "-"
           ->  '+' Primary                                                   
           ->  'not' Primary                                                 => "not"
           ->  'eof'
           ->  Name 
           ->  '<integer>'
           ->  '<char>'
           ->  Name '(' Expression list ',' ')'                              => "call"      
           ->  '(' Expression ')'
           ->  'succ' '(' Expression ')'                                     => "succ"
           ->  'pred' '(' Expression ')'                                     => "pred"
           ->  'chr' '(' Expression ')'                                      => "chr"
           ->  'ord' '(' Expression ')'                                      => "ord";
  
Name       ->  '<identifier>';
 
</pre>
