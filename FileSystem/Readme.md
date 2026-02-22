# File System (LLD)

## Requirements

1. Hierarchical file system with single root directory
2. Files store string content
3. Folders contain files and other folders
4. Create and delete files and folders
5. List contents of a folder
6. Navigate/resolve absolute paths (e.g. `/home/users/docs`)
7. Rename and move files and folders
8. Retrieve full path from any file/folder reference
9. Scale to tens of thousands of entries in memory

### Out of Scope

1. Search functionality
2. Relative path resolution (`../` or `./`)
3. Permissions, ownership, timestamps
4. File type-specific behaviour
5. Persistence / disk storage
6. Symbolic links
7. UI layer

---

## Entities

- **FileSystem:** The orchestrator. Owns the root folder, parses paths, and provides the public API for all operations. External code interacts with this class, never directly with folders or files.
- **Folder:** Represents a directory. Has a name, contains child entries (files or other folders), and provides methods to add, remove, and look up children. Doesn't know about paths or the broader tree structure.
- **File:** Represents a file. Has a name and stores content. A leaf node with no children.

### Structure

```
FileSystem
    └── root: Folder
            ├── Folder ("home")
            │       └── Folder ("user")
            │               ├── File ("notes.txt")
            │               └── Folder ("docs")
            └── File ("readme.txt")
```

---

## Class Design

### FileSystem

```
class FileSystem:
  - root: Folder

  - FileSystem()
  + createFile(path, content) -> File
  + createFolder(path) -> Folder
  + move(srcPath, destPath) -> bool or throws
  + delete(path) -> bool or throws
  + get(path) -> FileSystemEntry or throws
  + list(path) -> list of entries or throws
  + rename(path, newName)
```

### Path storage: bad vs good design

**Bad design: store path as string**

Each entry stores its full path as a string field:

```
class File:
    - name: string
    - path: string   // e.g. "/home/user/notes.txt"
    - content: string
```

`getPath()` just returns the stored string. Simple and O(1).

**Challenges:** When you rename a folder (e.g. `/home` → `/house`), you must update the path of every entry underneath it. Rename/move become O(n) in the number of descendants.

```
rename("/home", "house")
  → update "/home" to "/house"
  → update "/home/user" to "/house/user"
  → update "/home/user/docs" to "/house/user/docs"
  → update "/home/user/docs/report.txt" to "/house/user/docs/report.txt"
  → ... hundreds more ...
```

**Good design: store parent pointer**

Each entry stores a reference to its parent folder. `getPath()` walks up via parent pointers to the root, collecting names:

```
class File:
    - name: string
    - parent: Folder?
    - content: string

getPath():
    if parent == null
        return name   // Root returns "/"
    parentPath = parent.getPath()
    if parentPath == "/"
        return "/" + name
    else
        return parentPath + "/" + name
```

### Core classes

```
abstract class FileSystemEntry:
    - name: string
    - parent: Folder?

    + getName() -> string
    + getParent() -> Folder?
    + setParent(Folder?)
    + getPath() -> string
    + isDirectory() -> boolean   // abstract

class File extends FileSystemEntry:
    - content: string
    + File(name, content)
    + getName() -> string
    + setName(name)
    + getContent() -> string
    + setContent(content)
    + getParent() -> Folder?
    + setParent(Folder?)
    + getPath() -> string
    + isDirectory() -> false

class Folder extends FileSystemEntry:
    - children: Map<string, FileSystemEntry>
    + Folder(name)
    + getName() -> string
    + setName(name)
    + getParent() -> Folder?
    + setParent(Folder?)
    + getPath() -> string
    + isDirectory() -> true
    + addChild(entry) -> boolean
    + removeChild(name) -> FileSystemEntry
    + getChild(name) -> FileSystemEntry
    + hasChild(name) -> boolean
    + getChildren() -> List<FileSystemEntry>
```

---

## Implementation

### FileSystem

```text
createFile(path, content)
    if path == "/"
        throw InvalidPathException("Cannot create file at root")
    parent = resolveParent(path)
    fileName = extractName(path)   // "/home/user/notes.txt" -> "notes.txt"
    if parent.hasChild(fileName)
        throw AlreadyExistsException("Entry already exists: " + fileName)
    file = File(fileName, content)
    parent.addChild(file)
    return file
```

```text
createFolder(path)
    if path == "/"
        throw AlreadyExistsException("Root already exists")
    parent = resolveParent(path)
    folderName = extractName(path)
    if parent.hasChild(folderName)
        throw AlreadyExistsException("Entry already exists: " + folderName)
    folder = Folder(folderName)
    parent.addChild(folder)
    return folder
```

```text
get(path)
    return resolvePath(path)
```

```text
list(path)
    entry = resolvePath(path)
    if !entry.isDirectory()
        throw NotADirectoryException("Cannot list a file")
    return entry.getChildren()
```

```text
delete(path)
    if path == "/"
        throw InvalidPathException("Cannot delete root")
    parent = resolveParent(path)
    name = extractName(path)
    removed = parent.removeChild(name)
    if removed == null
        throw NotFoundException("Entry not found: " + path)
```

*Note: Deletion of non-empty folders is allowed; the entire subtree is removed. To require empty folders (rmdir-style), add: `if entry.isDirectory() && !entry.getChildren().isEmpty() throw error`.*

```text
rename(path, newName)
    // Rename keeps entry in same location; name-only change.
    if path == "/"
        throw InvalidPathException("Cannot rename root")
    parent = resolveParent(path)
    oldName = extractName(path)
    if !parent.hasChild(oldName)
        throw NotFoundException("Cannot rename non-existent path")
    if parent.hasChild(newName)
        throw AlreadyExistsException("Name already exists")
    entry = parent.removeChild(oldName)
    entry.setName(newName)
    parent.addChild(entry)
```

*Note: We remove the entry (keyed by old name), set the new name, then re-add so the parent’s map stays correct.*

```text
move(srcPath, destPath)
    if srcPath == "/"
        throw InvalidPathException("Cannot move root")
    srcParent = resolveParent(srcPath)
    srcName = extractName(srcPath)
    entry = srcParent.getChild(srcName)
    if entry == null
        throw NotFoundException("Source not found: " + srcPath)
    destParent = resolveParent(destPath)
    destName = extractName(destPath)
    // Cycle: cannot move folder into itself or a descendant
    if entry.isDirectory()
        current = destParent
        while current != null
            if current == entry
                throw InvalidPathException("Cannot move folder into itself")
            current = current.getParent()
    if destParent.hasChild(destName)
        throw AlreadyExistsException("Destination already exists: " + destPath)
    srcParent.removeChild(srcName)
    entry.setName(destName)
    destParent.addChild(entry)
```

```text
resolvePath(path)
    if path == null or path is empty
        throw InvalidPathException("Invalid path")
    if !path.startsWith("/")
        throw InvalidPathException("Path must be absolute")
    if path == "/"
        return root
    // Split "/home/user/docs" into ["home", "user", "docs"]
    parts = path.substring(1).split("/")
    current = root
    for part in parts
        if part is empty
            throw InvalidPathException("Invalid path: consecutive slashes")
        if !current.isDirectory()
            throw NotADirectoryException
        child = current.getChild(part)
        if child == null
            throw NotFoundException("Path not found: " + path)
        current = child
    return current
```

*Path parsing is intentionally simple (no `../`, symlinks, etc.) per out-of-scope.*

```text
resolveParent(path)
    if path == "/"
        throw InvalidPathException("Root has no parent")
    lastSlash = path.lastIndexOf("/")
    parentPath = (lastSlash == 0) ? "/" : path.substring(0, lastSlash)
    parent = resolvePath(parentPath)
    if !parent.isDirectory()
        throw NotADirectoryException
    return parent
```

```text
extractName(path)
    lastSlash = path.lastIndexOf("/")
    return path.substring(lastSlash + 1)
```

### FileSystemEntry.getPath

```text
getPath()
    if parent == null
        return name   // root returns "/"
    parentPath = parent.getPath()
    if parentPath == "/"
        return "/" + name
    else
        return parentPath + "/" + name
```

### Folder

```text
addChild(entry)
    if entry == null
        return false
    if children.containsKey(entry.getName())
        return false
    children.put(entry.getName(), entry)
    entry.setParent(this)
    return true

removeChild(name)
    entry = children.remove(name)
    if entry != null
        entry.setParent(null)
    return entry

getChild(name)
    return children.get(name)

hasChild(name)
    return children.containsKey(name)

getChildren()
    return new List(children.values())
```

### File

```text
getContent()
    return content

setContent(newContent)
    content = newContent

isDirectory()
    return false
```

---

## Extensibility

### "How would you make this file system thread-safe?"

**Problem:** Two threads can create the same path and corrupt the tree:

- Thread A: `createFile("/home/notes.txt", "hello")`
- Thread B: `createFile("/home/notes.txt", "world")`

Both can pass `hasChild("notes.txt")` and then both add a child.

**Solution 1: Coarse-grained locking**

```text
createFile(path, content)
    synchronized(this)
        parent = resolveParent(path)
        fileName = extractName(path)
        if parent.hasChild(fileName)
            throw AlreadyExistsException
        file = File(fileName, content)
        parent.addChild(file)
        return file
```

One thread runs any FileSystem method at a time. Correct but limits concurrency (e.g. creates in different folders still block each other).

**Solution 2: Fine-grained locking (per-folder)**

```text
createFile(path, content)
    parent = resolveParent(path)
    fileName = extractName(path)
    synchronized(parent)
        if parent.hasChild(fileName)
            throw AlreadyExistsException
        file = File(fileName, content)
        parent.addChild(file)
    return file
```

Creates in different folders can run in parallel. For **move**, two folders are involved → risk of deadlock. Fix: **lock ordering** (e.g. by path string):

```text
move(srcPath, destPath)
    srcParent = resolveParent(srcPath)
    destParent = resolveParent(destPath)
    firstLock = (srcParent.getPath() < destParent.getPath()) ? srcParent : destParent
    secondLock = (srcParent.getPath() < destParent.getPath()) ? destParent : srcParent
    synchronized(firstLock)
        synchronized(secondLock)
            // cycle check, collision check, then remove + set name + addChild
```

**Read-write locks:** Use a read-write lock so `get`/`list` can run concurrently; `createFile`, `delete`, `move`, `rename` hold the write lock.

---

### "How would you add search functionality?"

Without search, finding a file by name requires traversing the whole tree — O(n).

**Bad (traversal):** Recursively visit every entry and match name. Correct but O(n) per search.

**Good (index):** Trade space for time. Maintain `nameIndex: Map<String, List<FileSystemEntry>>`. On create, add to index; on delete, remove; on rename, remove from old name list and add to new name list. Then:

```text
search(name)
    if nameIndex.containsKey(name)
        return nameIndex.get(name)
    return []
```

Search by exact name becomes O(1). Extensions:

1. **Prefix search:** Use a trie keyed by name; descend to prefix and collect descendants.
2. **Wildcards (e.g. `*.txt`):** Secondary index by extension or traverse with pattern match.
3. **Content search:** Inverted index (word → files) — more involved.
