# Agent Tool Calling Guidelines

This document outlines the general guidelines and best practices for Agents when performing Tool Calling. Strict adherence to these rules ensures that tools are executed correctly, prevents underlying tool-call syntax tags from leaking into the user interface, and improves overall problem-solving efficiency.

## 1. Core Principles of Tool Calling

### 1.1 Strict Schema Compliance
* **Carefully Read Tool Declarations**: Before invoking any tool, you must consult the provided API declarations and their JSON Schema in the current system context.
* **Exact Parameter Matching**: Never guess parameter names or tool names based on intuition. For example, if the declared parameter for searching files is `query`, you must never replace it with `filter`, `path`, or any other synonym.
* **Data Types and Required Fields**: Ensure that all parameters in the `required` list are provided, and that their data types (`STRING`, `INTEGER`, `BOOLEAN`, `ARRAY`, `OBJECT`) strictly match the declaration.

### 1.2 No Hallucination
* **Use Only Declared Tools**: Call only the tools explicitly provided in your environment. If a specific operation lacks a dedicated tool, prioritize using the provided alternatives. Never attempt to call a non-existent API.
* **Do Not Fabricate Results**: Subsequent actions must be based solely on the actual output returned by the tool execution. Do not assume or fabricate file contents, command outputs, or execution states.

### 1.3 Accurate Syntax
* Ensure that the Tool Calling syntax strictly follows the system's defined format (e.g., `call:function_name{...}`). Formatting errors lead to parsing failures, which typically result in raw underlying control tags (like `<|tool_call|>`) being directly exposed in the user's UI.

## 2. File Operation and Modification Guidelines

### 2.1 Prioritize Specialized Tools
* **No Shell Scripts for File Edits**: It is **strictly prohibited** to use `run_shell_command` with commands like `sed`, `awk`, `perl`, or `echo >` to modify files directly. These operations bypass the IDE's memory buffers, destroy the user's unsaved changes, and cause data loss.
* **Use Standard APIs for Modification**: Always use the system-provided tools such as `write_file`, `replace_file_content`, or `multi_replace_file_content` to perform file modifications.
* **Shell as a Fallback for File System Operations**: For operations that do not have a dedicated tool (e.g., deleting files with `rm`, moving/renaming with `mv`, or creating directories with `mkdir`), you may use `run_shell_command`. 
    * **Precedence**: Always check if a specialized tool (like `list_files`, `find_files`, `grep`, `read_file`) can achieve your goal before falling back to the shell.
    * **Strict Constraint**: The "No Shell Edits" rule still applies—never use shell to change the *content* of a file.
    * **Caution**: When performing destructive operations like `rm` or `mv`, ensure the project state is stable and the operation is truly necessary.

### 2.2 The "Read Before Write" Principle
* Before modifying any file, you **must** use tools like `read_file` to view its actual, current content. Never blindly replace file contents based on generic templates or prior knowledge.

### 2.3 Precise Replacement Matching
* When using targeted replacement tools (like `replace_file_content`), the provided `targetContent` must **exactly match** the text in the original file, including indentation, spaces, and line breaks. Utilize `startLine` and `endLine` parameters to narrow down the search scope and ensure the accuracy of your modifications.

## 3. Error Handling and Debugging Strategy

### 3.1 Responding to Errors
* **No Blind Retries**: If a tool call returns an error, analyze the error message first (e.g., missing parameter, type mismatch, unmatched target content). Do not repeatedly call the tool using the same incorrect parameters.
* **Adjust Strategies**: If a content search fails (e.g., `grep` or `find_files` returns no results), try broadening your search conditions or utilizing alternative search tools (like `code_search` or `find_declaration`) to locate the target.

## 4. Conclusion

Every Tool Call is a rigorous API interaction with the system. During the reasoning phase of generating a call instruction, an Agent must silently verify that its intentions fully align with the tool declarations in the current context. Only through precise, restrained, and rigorous use of tools can an Agent provide the most efficient development assistance to the user.