# IntelliJ 平台能力与兼容基线

- Type: research
- Status: resolved
- Blocked by: none

## Question

IntelliJ Platform 当前哪些公开 API 能稳定取得编辑器选区与源文件、提供选区后的批注入口和输入界面、写入系统剪贴板；本插件应采用什么 Gradle、JDK、IDE 构建号与最低兼容版本？

## Resolution

### 平台能力

- **取得选区**：在 `AnAction` 中通过 `CommonDataKeys.EDITOR` 取得 `Editor`，用 `SelectionModel.hasSelection()` 控制入口可见性，并用 `getSelectedText()` 读取文本。这是 JetBrains 官方编辑器 Action 示例采用的公开路径：[Working with Text](https://plugins.jetbrains.com/docs/intellij/working-with-text.html)。
- **取得来源**：优先从当前 `Document` 调用 `FileDocumentManager.getFile()` 得到 `VirtualFile`；Action 场景也可读 `PlatformDataKeys.VIRTUAL_FILE`。官方 VFS 文档明确列出这两条路径：[Virtual Files](https://plugins.jetbrains.com/docs/intellij/virtual-file.html)。绝对路径可从本地文件对应的 `VirtualFile` 取得；无真实文件的 Document 可能返回 `null`，具体产品行为交给“批注来源定位规则”决策。
- **批注入口**：把 `AnAction` 注册到 `EditorPopupMenu` 是官方示例覆盖的最小稳定方案。若要接近 Codex 截图中的选区旁浮层，可用 `JBPopupFactory.createComponentPopupBuilder()` 构建自定义 Popup，并显式定位到编辑器坐标：[Popups](https://plugins.jetbrains.com/docs/intellij/popups.html)。根据官方能力边界推断，平台没有自动复刻 Codex 选区工具条的单一高层 API，浮层方案需要额外监听选区变化和管理 Popup 生命周期。
- **输入界面**：`DialogWrapper` 原生提供 OK/Cancel、焦点、Esc、尺寸记忆和校验，是最小模态实现：[Dialogs](https://plugins.jetbrains.com/docs/intellij/dialog-wrapper.html)。自定义 Popup 可承载多行文本组件，视觉更接近截图，但行为复杂度更高。
- **剪贴板**：调用公开的 `CopyPasteManager.copyTextToClipboard(String)` 即可写入系统剪贴板；实现会在 IDE 中委托给平台剪贴板服务：[JetBrains 平台源码](https://github.com/JetBrains/intellij-community/blob/master/platform/editor-ui-api/src/com/intellij/openapi/ide/CopyPasteManager.java)。

### 构建与兼容基线

- 本机环境确认为 IntelliJ IDEA `2026.2.1`、构建 `IU-262.9437.185`，证据为 `/Applications/IntelliJ IDEA.app/Contents/Resources/product-info.json` 和 `build.txt`。
- 首版开发基线采用 IntelliJ IDEA `2026.2.0.1`、`sinceBuild = "262"`。JetBrains 当前依赖示例直接使用 `intellijIdea("2026.2.0.1")`：[Dependencies Extension](https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin-dependencies-extension.html)。最终支持范围仍由“支持范围与交付边界”决定。
- 使用 IntelliJ Platform Gradle Plugin `2.18.1` 和 Gradle Wrapper `9.x`；该插件最低要求 Gradle `9.0.0`：[Gradle Plugin 2.x](https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html)。
- 2026.2+ 平台插件代码使用 Java `25`；JetBrains 已明确该版本边界：[Incompatible Changes](https://plugins.jetbrains.com/docs/intellij/api-changes-list-2025.html)。本机 IDEA 也随附 JBR `25.0.3`。
- 当前所需能力都属于平台模块，`plugin.xml` 只需依赖 `com.intellij.modules.platform`，不需要 Java 插件 API。暂不设置 `untilBuild`；正式扩展兼容范围前应按最低支持版本编译并使用 Plugin Verifier 检查：[Build Number Ranges](https://plugins.jetbrains.com/docs/intellij/build-number-ranges.html)。

结论：公开 API 能完整支撑本需求，不需要内部或实验性 API。下一步可以在“右键菜单 + DialogWrapper”和“选区旁 Popup”之间制作最小交互原型。
