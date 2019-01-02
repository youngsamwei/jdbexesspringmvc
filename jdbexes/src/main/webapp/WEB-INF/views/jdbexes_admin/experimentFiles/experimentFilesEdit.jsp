<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/commons/global.jsp" %>

	<script type="text/javascript" src="${staticPath }/static/easyui/ckeditor/ckeditor.js"></script>
	<script type="text/javascript" src="${staticPath }/static/easyui/ckeditor/adapters/jquery.js"></script>

<script type="text/javascript">
    $(function() {
        $('#experimentFilesEditForm').form({
            url : '${path }/dbexpfiles/edit',
            onSubmit : function() {
                progressLoad();
                var isValid = $(this).form('validate');
                if (!isValid) {
                    progressClose();
                }
                return isValid;
            },
            success : function(result) {
                progressClose();
                result = $.parseJSON(result);
                if (result.success) {
                    parent.$.modalDialog.openner_dataGrid.datagrid('reload');//之所以能在这里调用到parent.$.modalDialog.openner_dataGrid这个对象，是因为user.jsp页面预定义好了
                    parent.$.modalDialog.handler.dialog('close');
                } else {
                    var form = $('#experimentFilesEditForm');
                    parent.$.messager.alert('错误', eval(result.msg), 'error');
                }
            }
        });


    });
</script>
<div class="easyui-layout" data-options="fit:true,border:false">
    <div data-options="region:'center',border:false" title="" style="overflow: hidden;padding: 3px;">
        <form id="experimentFilesEditForm" style="height:100%" method="post">
            <table style="height:100%" class="grid">

                <tr>
                    <td width="100">源文件名称</td>
                    <td >
                     <input name="fileno" type="hidden"  value="${experimentFiles.fileno}">
                         <input name="srcfilename" type="text" placeholder="源文件名称" class="easyui-validatebox" style="width:100%" data-options="required:true" value="${experimentFiles.srcfilename}">
                    </td>
                </tr>

                <tr  style="height:90%">
                    <td>目标文件名称</td>
                    <td > <input name="dstfilename" type="text" placeholder="目标文件名称" class="easyui-validatebox" style="width:100%" data-options=" required:true" value="${experimentFiles.dstfilename}">                 </td>
                </tr>

            </table>
        </form>
    </div>
</div>