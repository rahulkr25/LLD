package FileSystem;

import java.util.List;

import FileSystem.exceptions.EntityExistsException;
import FileSystem.exceptions.InvalidPathException;
import FileSystem.exceptions.NotADirectoryException;
import FileSystem.exceptions.EntityNotFoundException;

public class FileSystem {
    Folder root;
    
    public FileSystem() {
        root = new Folder("/");
        root.setParent(null);
    }

    public boolean createFile(String path, String content){

        if(path.isBlank() || path.isEmpty()){
            throw new InvalidPathException("String is blank or empty");
        }
        if(path == "/"){
            throw new InvalidPathException("Cannot create files at root directory");
        }

        Folder parent = resolveParent(path);
        String name = extractName(path);
        synchronized(parent){
        if(parent.getChildren(name)!= null){
            throw new EntityExistsException("entity already exists");
        }

        File file = new File(name, content);
        parent.addChildren(file);
        return true;
       }
    }
    //. /home/user/docs
    private Folder resolveParent(String path){
        if(path == "/")
            return root;

        int lastSlash = path.lastIndexOf("/");
        String parentPath = lastSlash == 0 ? "/": path.substring(0,lastSlash);
        FileSystemEntry parent = resolvePath(parentPath);
        if (!parent.isDirectory())
           throw new NotADirectoryException("Not a directory" + parentPath);

        return (Folder) parent;

    }

    private String extractName(String path){
         if(path == "/")
            return "/";
          int lastSlash = path.lastIndexOf("/");
          return path.substring(lastSlash+1);
    }

    private FileSystemEntry resolvePath(String path){
        //. /home/user/docs
        if (path.isBlank() || path.isEmpty()){
            throw new InvalidPathException("invalid path: "+ path);
        }
        if (!path.startsWith("/")){
            throw new  InvalidPathException("Path must be absolute: "+ path);
        }
        if(path == "/")
            return root;

        String [] parts = path.substring(1).split("/");

        Folder current = root;

        for (String part: parts){
            if (part.isBlank() || part.isEmpty())
                throw new InvalidPathException("invalid path: "+ path);

            if (!current.isDirectory()){
                throw new NotADirectoryException(path);
            }
            FileSystemEntry child = current.getChildren(part);
            if(child==null){
                throw new EntityNotFoundException(part);
            }
            current = (Folder)child;
        }
        return current;

    }

    public boolean createFolder(String path){
         if(path.isBlank() || path.isEmpty()){
            throw new InvalidPathException("String is blank or empty");
        }
        if(path == "/")
            throw new InvalidPathException("Cannot create root");

        Folder parent = resolveParent(path);
        String name = extractName(path);

        if(parent.getChildren(name)!= null){
            throw new EntityExistsException("entity already exists");
        }

        Folder folder = new Folder(name);
        parent.addChildren(folder);
        return true;
    }

    //. /home/user/docs -> /home/user/post (Allowed)
    //. /home  -> /home/user/docs/ (not allowed)
    public boolean move(String srcPath, String destPath){
        if(srcPath.isBlank() || srcPath.isEmpty() ||
             destPath.isBlank() || destPath.isEmpty()  ){
            throw new InvalidPathException("String is blank or empty");
        }

        Folder srcParent = resolveParent(srcPath);
        String srcName = extractName(srcPath);
        Folder destParent = resolveParent(destPath);
        String destName = extractName(destPath);

        // Always lock in alphabetical order by path
       Folder firstLock = srcParent.getPath().compareTo(destParent.getPath()) < 0 ? srcParent : destParent;
       Folder secondLock = srcParent.getPath().compareTo(destParent.getPath()) < 0 ? destParent : srcParent;

        synchronized(firstLock){
            synchronized(secondLock){
                FileSystemEntry entry = srcParent.getChildren(srcName);
                if(entry == null){
                    throw new EntityNotFoundException("Source not found: "+ srcPath);
                }

            
                if(destParent.hasChild(destName)){
                    throw new EntityExistsException(destName);
                }

                if(entry.isDirectory()){
                    Folder current = destParent;
                    while(current != null){
                        if(current == entry){
                            throw new InvalidPathException("Cannot move folder to itself");
                        }
                        current = current.getParent();
                    }
                }

                srcParent.removeChild(srcName);
                entry.setName(destName);
                destParent.addChildren(entry);
                return true;
         }
       }
    }

    public FileSystemEntry get(String path){
        return resolvePath(path);
    }

    public List<FileSystemEntry> list(String path){
        FileSystemEntry entry = resolvePath(path);
        if (entry == null || !entry.isDirectory()){
            throw new EntityNotFoundException("entity not found");
        }
        return ((Folder)entry).getChildrenList();
    }

    //. /home/user/docs -> post
    public void rename(String srcPath, String newName){
        if(srcPath.isBlank() || srcPath.isEmpty() ||
             newName.isBlank() || newName.isEmpty()  ){
            throw new InvalidPathException("String is blank or empty");
        }

        Folder srcParent = resolveParent(srcPath);
        String srcName = extractName(srcPath);
        FileSystemEntry entry = srcParent.getChildren(srcName);
        if(entry == null){
            throw new EntityNotFoundException("Source not found: "+ srcPath);
        }

        if(srcParent.hasChild(newName)){
            throw new EntityExistsException("new path already exists: "+ newName);
        }

        srcParent.removeChild(srcName);
        entry.setName(newName);
        srcParent.addChildren(entry);
    }

    public void delete(String path){
          if(path.isBlank() || path.isEmpty()){
            throw new InvalidPathException("String is blank or empty");
        }

        Folder parent = resolveParent(path);
        String name = extractName(path);

        FileSystemEntry removed = parent.removeChild(name);
        if(removed == null){
            throw new EntityNotFoundException(path);
        }
        
    }


}
