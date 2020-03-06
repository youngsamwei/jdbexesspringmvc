<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/commons/global.jsp" %>

<script type="text/javascript">
    const experimentExpstuSubmit = function () {
        let datagrid = initDataGrid();
        let form = initForm();
        const uploadFiles = [];

        function initDataGrid() {
            return $('#experiment-stu-submit-datagrid').datagrid({
                url: '${path}/dbexperiment_stu/experimentFilesDataGrid?expstuno=${experimentStu.expstuno}',
                striped: true,
                rownumbers: true,
                pagination: true,
                singleSelect: false,
                idField: 'fileno',
                sortName: 'fileno',
                sortOrder: 'asc',
                pageSize: 20,
                frozenColumns: [[{
                    width: '300',
                    title: '文件名称',
                    field: 'srcfilename',
                    sortable: true
                }, {
                    width: '200',
                    title: '上传状态',
                    field: 'desc',
                    formatter: descRowFormatter
                }, {
                    width: '100',
                    title: '文件大小',
                    field: 'size'
                }, {
                    field: 'status',
                    title: '操作',
                    width: 200,
                    resizable: false,
                    formatter: statusRowFormatter
                }]],
                onLoadSuccess: function (data) {
                    $('.experiment_stu-easyui-linkbutton-submit-cancel').linkbutton({text: '取消'});
                }
            });
        }

        function initForm() {
            $('#experiment-stu-submit-form').form({
                url: '${path}/dbexperiment_stu/check',
                onSubmit: onSubmit,
                success: success
            });
        }

        function onSubmit() {
            console.log(1);
            progressLoad();

            const isValid = $(this).form('validate');
            if (!isValid) {
                progressClose();
            }

            if (uploadFiles.length <= 0) {
                progressClose();
                return false;
            } else {
                Upload();
            }

            return isValid;
        }

        function success(result) {
            console.log(2);
            progressClose();
            result = $.parseJSON(result);
            if (result.success) {
                // 能在这里调用到 openner_dataGrid 这个对象，是因为 user.jsp 页面预定义好了
                parent.$.modalDialog.openner_dataGrid.datagrid('reload');
                parent.$.modalDialog.handler.dialog('close');
            } else {
                parent.$.messager.alert('错误', eval(result.msg), 'error');
            }
        }

        function descRowFormatter(value, row, index) {
            if (value === '待上传') return value;
            if (row.submittime == null) return '未上传';
            return "上次上传时间:" + row.submittime;
        }

        function statusRowFormatter(value, row, index) {
            if (value !== 1) return '';
            return $.formatString('<a href="javascript:void(0)"' +
                ' class="experiment_stu-easyui-linkbutton-submit-cancel"' +
                ' data-options="plain:true,iconCls:\'fi-pencil icon-blue\'"' +
                ' onclick="experimentExpstuSubmit.cancelSubmit({0});">取消</a>', index);
        }

        /**
         * 处理取消上传事件
         */
        function cancelSubmit(rowindex) {
            const ds = datagrid.datagrid("getData");
            const filename = ds.rows[rowindex].srcfilename.toLowerCase();
            for (let j = 0; j < uploadFiles.length; j++) {
                if (uploadFiles[j].name === filename) {
                    uploadFiles.splice(j, 1);
                    break;
                }
            }
            datagrid.datagrid("updateRow", {
                index: parseInt(rowindex),
                row: {desc: '', size: '', status: 0}
            });
        }

        /**
         * 将文件添加至 uploadFiles 中
         */
        function AddFiles(files) {
            let err_str = "";
            for (let i = 0; i < files.length; i++) {
                const filename = files[i].name.toLowerCase();

                /* region 后缀名检验 */
                const extension = filename.substring(filename.lastIndexOf(".") + 1, filename.length); //后缀名
                const myarray = ['CPP', 'cpp', 'C', 'c'];
                if ($.inArray(extension, myarray) === -1) {
                    err_str += filename + "/";
                    continue;
                }
                /* endregion */

                /* region 替换已有文件 */
                for (let j = 0; j < uploadFiles.length; j++) {
                    if (uploadFiles[j].name === filename) {
                        uploadFiles.splice(j, 1);
                        break;
                    }
                }
                /* endregion */

                const indexInGrid = findFilename(files[i]);
                if (indexInGrid === -1) continue;
                updateDataGrid(files[i], indexInGrid);
                uploadFiles.push(files[i]);
            }

            if (err_str !== "") console.error(err_str)
        }

        /**
         * 检查文件名是否在图表中存在
         * 若存在返回 index，否则返回 -1
         */
        function findFilename(file) {
            const info = datagrid.datagrid("getData");
            const name = file.name.toLowerCase();

            for (let i = 0; i < info.rows.length; i++) {
                if (name === info.rows[i].srcfilename.toLowerCase()) {
                    return i;
                }
            }
            return -1;
        }

        /**
         * 根据文件信息更新数据视图
         */
        function updateDataGrid(file, index) {
            const filename = file.name.toLowerCase();
            const size = (file.size / 1000) + "k";
            const info = datagrid.datagrid("getData");

            datagrid.datagrid("updateRow", {
                index: index,
                row: {desc: '待上传', size: size, status: 1}
            });
            file.expstuno = info.rows[index].expstuno;
            file.fileno = info.rows[index].fileno;
        }

        /**
         * 将 uploadFiles 中的文件上传至后台
         */
        function Upload() {
            AddFiles([]);
            if (uploadFiles.length <= 0) {
                return;
            }

            const formData = new FormData();
            formData.append("file", uploadFiles[0]);
            formData.append("expstuno", uploadFiles[0].expstuno);
            formData.append("fileno", uploadFiles[0].fileno);

            //
            const xhr = new XMLHttpRequest();
            xhr.open("post", "${path}/dbexperiment_stu/uploadFile");
            xhr.onload = Upload; // 递归上传剩余文件
            uploadFiles.shift();
            xhr.send(formData);
        }

        /**
         * 处理拖拽事件
         */
        function onDrop(e) {
            e.preventDefault();
            if (e.dataTransfer.files.length === 0) return false;
            AddFiles(e.dataTransfer.files);
        }

        return {
            datagrid: datagrid,
            form: form,
            uploadFiles: uploadFiles,
            cancelSubmit: cancelSubmit,
            allowDrop: e => e.preventDefault(),
            onDrop: onDrop
        };
    }();

    window.experimentExpstuSubmit = experimentExpstuSubmit;
</script>

<style>
    .drag-and-drop-area {
        background-color: #888888;
        color: white;
        font-size: 20px;
        height: 120px;
        line-height: 50px;
        border: 3px dashed silver;
        text-align: center;
        display: block;
    }
</style>

<div class="easyui-layout" data-options="fit:true,border:false">
    <div data-options="region:'center',border:false" style="overflow: hidden;padding: 3px;">
        <table id="experiment-stu-submit-datagrid" data-options="fit:true,border:false"></table>
        <form id="experiment-stu-submit-form" method="post">
            <input name="expno" id="expno" type="hidden" value="${experimentStu.expno}">
        </form>
    </div>
    <div class="drag-and-drop-area" name="dropbox" id="dropbox" data-options="region:'south',border:false"
         ondrop="experimentExpstuSubmit.onDrop(event)" ondragover="experimentExpstuSubmit.allowDrop(event)">
        拖拽文件到此处准备上传<BR>
        请拖拽cpp文件，且与上面列表中的文件名称一致
    </div>
</div>