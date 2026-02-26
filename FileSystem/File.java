package FileSystem;

public class File extends FileSystemEntry {
    String content;

    public File(String name, String content){
        super(name);
        this.content = content;
    }
    @Override
    public boolean isDirectory(){
      return false;
    }
}
