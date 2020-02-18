<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/commons/global.jsp" %>

<script type="text/javascript">

    const experimentEdit = function () {
        const form = initForm();

        function initForm() {
            return $('#experiment-edit-form').form({
                url: '${path}/dbexp/edit',
                onSubmit: onSubmit,
                success: onSuccess
            });
        }

        function onSubmit() {
            progressLoad();
            const isValid = $(this).form('validate');
            if (!isValid) progressClose();
            return isValid;
        }

        function onSuccess(result) {
            progressClose();
            result = $.parseJSON(result);
            if (!result.success) {
                parent.$.messager.alert('错误', eval(result.msg), 'error');
                return;
            }
            parent.$.modalDialog.openner_dataGrid.datagrid('reload');
            parent.$.modalDialog.handler.dialog('close');
        }

        $("#experiment-edit-form-testtarget").val('${experiment.isOpen}');

        return {
            form: form
        }
    }();


</script>

<div class="easyui-layout" data-options="fit:true,border:false">
    <div data-options="region:'center',border:false" title="" style="overflow: hidden;padding: 3px;">
        <form id="experiment-edit-form" method="post">
            <table class="grid">
                <tr>
                    <td><label for="experiment-edit-form-expname">实验名称</label>></td>
                    <td><input name="expno" type="hidden" value="${experiment.expno}">
                        <input id="experiment-edit-form-expname" name="expname" type="text" placeholder="请输入实验名称"
                               class="easyui-validatebox" style="width: 300px; height: 20px;"
                               data-options="required:true" value="${experiment.expname}"></td>
                </tr>
                <tr>
                    <td><label for="experiment-edit-form-testtarget">编译目标</label></td>
                    <td><input id="experiment-edit-form-testtarget" name="testtarget" type="text" placeholder="请输入编译目标"
                               class="easyui-validatebox" style="width: 300px; height: 20px;"
                               data-options="required:true" value="${experiment.testtarget}"></td>
                </tr>
                <tr>
                    <td><label for="experiment-edit-form-status">状态</label></td>
                    <td><select id="experiment-edit-form-status" name="isOpen" class="easyui-combobox"
                                data-options="width:140,height:29,editable:false,panelHeight:'auto'">
                        <option value="1">正常</option>
                        <option value="0">停用</option>
                    </select></td>
                </tr>

            </table>
        </form>
    </div>
</div>