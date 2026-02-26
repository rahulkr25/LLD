package FileSystem;

public abstract class FileSystemEntry {
    String name;
    Folder parent;

    public FileSystemEntry(String name){
        this.name = name;
    }
      public FileSystemEntry(String name, Folder parent){
        this.name = name;
        this.parent = parent;
    }

    public void setName(String name){
        this.name = name;
    }
    public void setParent(Folder parent){
        this.parent = parent;
    }

    public Folder getParent(){
        return parent;
    }

    public String getName(){
        return name;
    }

    public String getPath(){
        if(parent == null){
            return "/";
        }

        if (parent.getPath() == "/"){
            return "/" + name;
        }
        return parent.getPath() + "/" + name;


    }
    public abstract boolean isDirectory();

    
}
