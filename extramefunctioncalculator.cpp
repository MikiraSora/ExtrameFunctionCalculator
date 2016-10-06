#include "extramefunctioncalculator.h"

extrameFunctionCalculator::extrameFunctionCalculator(){
}

std::string extrameFunctionCalculator::execute(std::string text){
    if(text.empty())
        throw std::runtime_error("input empty text to execute");
    std::string executeType,paramter,result;
    for(int position = 0;position<text.length();position++){
        if(text[position] == ' '){
            paramter = text.substr(position+1);
        }else{
            executeType +=text[position];
        }
    }

    /*an ideal way to process your executeType is to create a map
     * to connect itself with execute function, like <str,function>;
     * @racaljk
     */
    if(executeType.compare("set")==0){
    }else if(executeType.compare("set_expr")==0){

    }else if(executeType.compare("reg")==0){

    }else if(executeType.compare("solve")==0){

    }else if(executeType.compare("dump")==0){

    }else if(executeType.compare("help")==0){

    }else if(executeType.compare("load")==0){

    }else if(executeType.compare("clear")==0){

    }else if(executeType.compare("reset")==0){

    }else if(executeType.compare("save")==0){

    }else if(executeType.compare("delete")==0){

    }else{
        throw std::runtime_error("unknown execution other "+executeType);
    }

    return result;
}


