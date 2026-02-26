package FileSystem.exceptions;

public class EntityNotFoundException extends RuntimeException{
public EntityNotFoundException(String mesg){
    super(mesg);
}
}