package FileSystem;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.List;
import FileSystem.exceptions.EntityNotFoundException;
import FileSystem.exceptions.EntityExistsException;
public class Folder extends FileSystemEntry {
    Map<String,FileSystemEntry>children;
   
    public Folder(String name){
        super(name);
        this.children = new HashMap<>();
    }
    
    @Override
    public boolean isDirectory(){
      return true;
    }
    public FileSystemEntry getChildren(String name){
        return children.get(name);  
    }
    public boolean hasChild(String name){
        return children.containsKey(name);
    }
    public FileSystemEntry removeChild(String name){
        FileSystemEntry entry = children.remove(name);
        if (entry != null){
            entry.setParent(null);
        }
        return entry;
    }

    public List<FileSystemEntry> getChildrenList(){
        return children.values().stream().collect(Collectors.toList());
    }
    public boolean addChildren(FileSystemEntry entry){
        if (entry == null){
            throw new EntityNotFoundException("entity not found");
        }
        if(children.containsKey(entry.getName())){
            throw new EntityExistsException("entity already exists");
        }
        children.put(entry.getName(), entry);
        entry.setParent(this);
        return true;
    }
    
}
