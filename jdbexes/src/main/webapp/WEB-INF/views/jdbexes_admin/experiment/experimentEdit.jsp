<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/commons/global.jsp" %>
<script type="text/javascript">
    $(function() {
        $('#experimentEditForm').form({
            url : '${path }/dbexp/edit',
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
                    var form = $('#experimentEditForm');
                    parent.$.messager.alert('错误', eval(result.msg), 'error');
                }
            }
        });

        $("#experimentEditStatus").val('${experiment.isOpen}');
    });
</script>
<div class="easyui-layout" data-options="fit:true,border:false">
    <div data-options="region:'center',border:false" title="" style="overflow: hidden;padding: 3px;">
        <form id="experimentEditForm" method="post">
            <table class="grid">
                <tr>
                    <td>实验名称</td>
                    <td><input name="expno" type="hidden"  value="${experiment.expno}">
                    <input name="expname" type="text" placeholder="请输入实验名称" class="easyui-validatebox" data-options="width:200,height:29, required:true" value="${experiment.expname}"></td>
                </tr>

                <tr>
                    <td>状态</td>
                    <td >
                        <select id="experimentEditStatus" name="isOpen" class="easyui-combobox" data-options="width:140,height:29,editable:false,panelHeight:'auto'">
                            <option value="1">正常</option>
                            <option value="0">停用</option>
                        </select>
                    </td>
                </tr>

            </table>
        </form>
    </div>
</div>