$(document).ready(function()
{

    $(".navbar-form").on("submit", function()
    {
        var uriOrLabel = $(this).find("input[name=label]").val();
        if (uriOrLabel.indexOf("http://") === 0 || uriOrLabel.indexOf("https://") === 0)
        {
            $(this).attr("action", "");
            $(this).find("input[name=label]").attr("name", "uri");
        }
        
        return true;
    });
    
    $(".btn-delete").on("click", function() // prompt on DELETE
    {        
        return confirm('Are you sure?');
    });

    $(".btn-remove").on("click", function()
    {        
        return $(this).parent().parent().remove();
    });

    $("div.btn-group:has(div.btn.dropdown-toggle)").on("click", function()
    {
        $(this).toggleClass("open");
        
        return true;
    });

    $(".btn-add").on("click", function()
    {
        var clone = $(this).parent().parent().clone(true, true);
        var uuid = "uuid" + generateUUID();
        var input = clone.find("input[name='ou'],input[name='ob'],input[name='ol']");
        input.attr("id", uuid);
        input.val("");
        clone.find("label").attr("for", uuid);
        return $(this).parent().parent().after(clone);
    });

});